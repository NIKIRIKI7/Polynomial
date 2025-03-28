package math;

import java.util.*;
import static java.util.stream.Collectors.toList;

public class NewtonPolynomial extends Polynomial {
    private final List<Point> points = new ArrayList<>();
    private double[] dividedDifferences; // Хранит разделенные разности для эффективного вычисления
    private double[] xValues; // Сохраняем x-значения для быстрого вычисления
    private boolean needsRebuild = true; // Флаг для ленивого перестроения

    /**
     * Создает пустой полином Ньютона.
     */
    public NewtonPolynomial() {
        super();
        dividedDifferences = new double[0];
        xValues = new double[0];
    }

    /**
     * Создает полином Ньютона на основе заданных точек.
     * 
     * @param points список точек для интерполяции
     */
    public NewtonPolynomial(List<Point> points) {
        super();
        if (points != null && !points.isEmpty()) {
            addPointsInternal(points, true);
        } else {
            dividedDifferences = new double[0];
            xValues = new double[0];
        }
    }

    /**
     * Возвращает копию списка точек полинома.
     * 
     * @return список точек
     */
    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }

    /**
     * Добавляет точку в полином.
     * 
     * @param point точка для добавления
     * @throws NullPointerException если точка равна null
     * @throws IllegalArgumentException если x-координата точки уже существует в полиноме
     */


    /*
    *Добавление точки в полином Ньютона:
    Валидация: Проверка на null и уникальность x (с учётом EPSILON).
    Сортировка: Бинарный поиск для вставки точки в упорядоченный список.
    Ленивое обновление: Установка флага needsRebuild = true без немедленного перестроения.
    Перестроение: Автоматическое обновление разделённых разностей и коэффициентов только при вызове evaluate()/getCoefficients().
    Преимущества:
    Эффективность при массовом добавлении точек (минимум пересчётов).
    Гарантия порядка узлов для корректных разделённых разностей.
    *
    * */
    public void addPoint(Point point) {
        Objects.requireNonNull(point, "Point cannot be null");
        addPointInternal(point);
    }

    private void addPointInternal(Point point) {
        // Эффективный поиск дублирующего x с использованием бинарного поиска для сортированного массива
        int insertPoint = Collections.binarySearch(
            points, 
            point, 
            Comparator.comparingDouble(Point::getX)
        );
        
        if (insertPoint >= 0) {
            throw new IllegalArgumentException("Duplicate x value: " + point.getX());
        }
        
        for (Point existingPoint : points) {
            if (Math.abs(existingPoint.getX() - point.getX()) < EPSILON) {
                throw new IllegalArgumentException("Duplicate x value: " + point.getX());
            }
        }
        
        points.add(-(insertPoint + 1), point); // Вставка с сохранением сортировки
        needsRebuild = true;
    }

    /**
     * Добавляет список точек в полином.
     * 
     * @param newPoints список точек для добавления
     * @throws IllegalArgumentException если список содержит точки с дублирующимися x-координатами
     */
    public void addPoints(List<Point> newPoints) {
        if (newPoints == null || newPoints.isEmpty()) {
            return;
        }
        
        addPointsInternal(newPoints, false);
    }
    
    private void addPointsInternal(List<Point> newPoints, boolean isConstructor) {
        Objects.requireNonNull(newPoints, "List cannot be null");
        if (newPoints.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("List cannot contain null values");
        }
        
        // Оптимизация: сортируем новые точки перед добавлением
        List<Point> sortedNewPoints = sortPointsByX(newPoints);
        
        // Проверяем уникальность сначала в отсортированном массиве новых точек
        checkForDuplicateXAfterSort(sortedNewPoints);
        
        // Если точек у нас еще нет, просто добавляем все
        if (points.isEmpty()) {
            points.addAll(sortedNewPoints);
            if (!isConstructor) {
                needsRebuild = true;
            }
            rebuildPolynomial();
            return;
        }
        
        // Иначе добавляем точки эффективно, сохраняя сортировку
        for (Point p : sortedNewPoints) {
            addPointInternal(p);
        }
        
        // Перестраиваем полином только один раз после всех вставок
        rebuildPolynomial();
    }

    /**
     * Удаляет точку из полинома.
     * 
     * @param point точка для удаления
     * @return true если точка была удалена, false если точка не была найдена
     * @throws NullPointerException если точка равна null
     */
    public boolean removePoint(Point point) {
        boolean removed = points.remove(Objects.requireNonNull(point));
        if (removed) {
            needsRebuild = true;
            rebuildPolynomial();
        }
        return removed;
    }

    /**
     * Вычисляет значение полинома Ньютона в точке x.
     * 
     * @param x точка, в которой вычисляется значение полинома
     * @return значение полинома в точке x
     */
    @Override
    public double evaluate(double x) {
        if (needsRebuild) {
            rebuildPolynomial();
        }
        
        if (points.isEmpty()) return 0.0;
        
        // Проверка на точное совпадение с узлом интерполяции
        for (int i = 0; i < points.size(); i++) {
            if (Math.abs(x - xValues[i]) < EPSILON) {
                return points.get(i).getY();
            }
        }
        
        // В остальных случаях используем базовый метод Polynomial.evaluate()
        return super.evaluate(x);
    }
    
    @Override
    public List<Double> getCoefficients() {
        if (needsRebuild) {
            rebuildPolynomial();
        }
        return super.getCoefficients();
    }

    private void rebuildPolynomial() {
        if (points.isEmpty()) {
            updateCoefficients(List.of(0.0));
            dividedDifferences = new double[0];
            xValues = new double[0];
            needsRebuild = false;
            return;
        }

        // Создаем массивы для x и y значений для быстрого доступа
        int n = points.size();
        xValues = new double[n];
        double[] yValues = new double[n];
        
        for (int i = 0; i < n; i++) {
            Point p = points.get(i);
            xValues[i] = p.getX();
            yValues[i] = p.getY();
        }

        // Вычисляем разделенные разности 
        dividedDifferences = computeDividedDifferencesOptimized(xValues, yValues);
        
        // Строим полином в стандартной форме
        buildStandardFormPolynomial();
        
        needsRebuild = false;
    }
    
    // Эффективное построение стандартной формы без множества умножений
    private void buildStandardFormPolynomial() {
        int n = points.size();
        
        // Если только одна точка, полином = константа
        if (n == 1) {
            updateCoefficients(List.of(points.get(0).getY()));
            return;
        }
        
        // Используем схему синтетического деления для более эффективного построения
        double[][] coeffs = new double[n][n];
        
        // Инициализация с коэффициентами Ньютона
        coeffs[0][0] = dividedDifferences[0];
        
        for (int i = 1; i < n; i++) {
            // Копируем предыдущий ряд
            System.arraycopy(coeffs[i-1], 0, coeffs[i], 1, i);
            
            // Первый коэффициент - это текущий коэффициент из разделенных разностей
            coeffs[i][0] = coeffs[i-1][0];
            
            // Применяем синтетическое деление для каждого члена
            for (int j = 1; j <= i; j++) {
                coeffs[i][j] -= xValues[i-1] * coeffs[i][j-1];
            }
            
            // Добавляем текущую разделенную разность
            coeffs[i][0] += dividedDifferences[i];
        }
        
        // Используем коэффициенты из последней строки для обновления полинома
        double[] result = new double[n];
        System.arraycopy(coeffs[n-1], 0, result, 0, n);
        
        updateCoefficients(Arrays.stream(result).boxed().collect(toList()));
    }

    /**
     * Вычисляет разделенные разности с использованием итеративного подхода без рекурсии.
     * 
     * Формула основана на определении разделенных разностей:
     * f[x_i] = f(x_i)
     * f[x_i, x_i+1, ..., x_i+k] = (f[x_i+1, ..., x_i+k] - f[x_i, ..., x_i+k-1]) / (x_i+k - x_i)
     * 
     * @param xValues массив x-координат точек
     * @param yValues массив y-координат точек
     * @return массив разделенных разностей
     */
    private double[] computeDividedDifferencesOptimized(double[] xValues, double[] yValues) {
        int n = xValues.length;
        double[] divDiff = new double[n];
        
        // Копируем y-значения в массив разделенных разностей
        System.arraycopy(yValues, 0, divDiff, 0, n);
        
        // Вычисляем разделенные разности (оптимизировано)
        for (int j = 1; j < n; j++) {
            for (int i = n - 1; i >= j; i--) {
                divDiff[i] = (divDiff[i] - divDiff[i-1]) / (xValues[i] - xValues[i-j]);
            }
        }
        
        return divDiff;
    }

    // Сохраняем для использования в toString
    private double[][] computeDividedDifferences(double[] xValues, double[] yValues) {
        int n = xValues.length;
        double[][] table = new double[n][n];

        // Заполняем первый столбец значениями y
        for (int i = 0; i < n; i++) {
            table[i][0] = yValues[i];
        }

        // Вместо классического рекурсивного подхода используем формулу Лагранжа
        // для построения таблицы разделенных разностей
        for (int k = 1; k < n; k++) {
            for (int i = 0; i < n - k; i++) {
                // Создаем подмассив x-значений для текущего диапазона [i, i+k]
                double[] subX = new double[k+1];
                double[] subY = new double[k+1];
                for (int idx = 0; idx <= k; idx++) {
                    subX[idx] = xValues[i + idx];
                    subY[idx] = yValues[i + idx];
                }
                
                // Вычисляем разделенную разность через формулу Лагранжа:
                // f[x_i, ..., x_{i+k}] = Σ (j = 0 до k) [ f(x_{i+j}) / product (l =0, l != j до k) {x_{i+j} - x_{i+l}}]
                double sum = 0.0;
                for (int j = 0; j <= k; j++) {
                    double term = subY[j];
                    double denominator = 1.0;
                    
                    for (int l = 0; l <= k; l++) {
                        if (l != j) {
                            denominator *= (subX[j] - subX[l]);
                        }
                    }
                    
                    term /= denominator;
                    sum += term;
                }
                
                // Сохраняем результат в таблице
                table[i][k] = sum;
            }
        }
        
        return table;
    }

    @Override
    public String toString() {
        if (needsRebuild) {
            rebuildPolynomial();
        }
        
        if (points.isEmpty()) {
            return "0.00";
        }

        return super.toString();
    }

    @Override
    public int degree() {
        // Степень полинома Ньютона равна n-1, где n - количество точек
        return points.size() > 0 ? points.size() - 1 : 0;
    }
}