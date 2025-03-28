package math;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Locale;

public class LagrangePolynomial extends Polynomial {
    private final List<Point> points;
    private double[] barycentricWeights;
    private boolean needsRebuild = false;

    /**
     * Создает полином Лагранжа для интерполяции заданных точек.
     * 
     * Метод Лагранжа позволяет построить многочлен степени n-1, проходящий через n точек.
     * Используется формула: L(x) = Σ(i=0 до n-1) y_i * l_i(x), 
     * где l_i(x) = Π(j=0 до n-1, j≠i) (x - x_j)/(x_i - x_j)
     * 
     * @param points точки для интерполяции
     * @throws NullPointerException если points равно null
     * @throws IllegalArgumentException если points пусто или содержит дублирующиеся значения x
     */
    public LagrangePolynomial(List<Point> points) {
        super();
        Objects.requireNonNull(points, "Points list cannot be null");
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be empty");
        }
        
        this.points = sortPointsByX(points);
        checkForDuplicateXAfterSort(this.points);
        
        computeBarycentricWeights();
        rebuildPolynomial();
    }

    /**
     * Вычисляет барицентрические веса для быстрой оценки.
     * Эти веса используются в барицентрической формуле для интерполяции Лагранжа.
     * 
     * Барицентрический вес w_i = 1 / Π(j=0 до n-1, j≠i) (x_i - x_j)
     * Позволяет ускорить вычисление значений полинома Лагранжа.
     */
    private void computeBarycentricWeights() {
        int n = points.size();
        barycentricWeights = new double[n];
        
        for (int i = 0; i < n; i++) {
            double weight = 1.0;
            double xi = points.get(i).getX();
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    weight *= (xi - points.get(j).getX());
                }
            }
            
            barycentricWeights[i] = 1.0 / weight;
        }
    }

    /**
     * Проверяет, имеют ли все точки одинаковое значение y.
     * 
     * @return true если все точки имеют одинаковое значение y, иначе false
     */
    private boolean hasIdenticalYValues() {
        if (points.isEmpty()) return true;
        
        double firstY = points.get(0).getY();
        for (int i = 1; i < points.size(); i++) {
            if (Math.abs(points.get(i).getY() - firstY) > EPSILON) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Перестраивает коэффициенты полинома при необходимости.
     */
    private void rebuildPolynomial() {
        Polynomial lagrangePoly = buildLagrangePolynomial();
        updateCoefficients(lagrangePoly.getCoefficients());
        needsRebuild = false;
    }

    /**
     * Строит интерполяционный полином Лагранжа.
     * 
     * Основная формула: L(x) = Σ(i=0 до n-1) y_i * l_i(x)
     * где l_i(x) - базисные полиномы Лагранжа: l_i(x) = Π(j=0 до n-1, j≠i) (x - x_j)/(x_i - x_j)
     * 
     * @return Polynomial представляющий интерполяцию Лагранжа
     */
    private Polynomial buildLagrangePolynomial() {
        int n = points.size();
        
        // Оптимизация линейного случая (2 точки)
        if (n == 2) {
            double x0 = points.get(0).getX();
            double y0 = points.get(0).getY();
            double x1 = points.get(1).getX();
            double y1 = points.get(1).getY();
            
            double slope = (y1 - y0) / (x1 - x0);
            return new Polynomial(y0 - slope * x0, slope);
        } 
        
        // Особый случай для одинаковых значений y (константный полином)
        if (hasIdenticalYValues()) {
            return new Polynomial(points.get(0).getY());
        }
        
        // Обычный расчет для всех других случаев
        Polynomial result = new Polynomial();
        
        for (int i = 0; i < n; i++) {
            Point pi = points.get(i);
            double yi = pi.getY();
            
            // Строим базисный полином Лагранжа L_i(x)
            Polynomial basis = new Polynomial(1.0);
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    double xj = points.get(j).getX();
                    double xi = pi.getX();
                    
                    // Умножаем на (x - xj)/(xi - xj)
                    double weight = 1.0 / (xi - xj);
                    Polynomial term = new Polynomial(-xj * weight, weight);
                    basis = basis.multiply(term);
                }
            }
            
            // Добавляем yi * L_i(x) к результату
            result = result.add(basis.multiply(yi));
        }
        
        return result;
    }

    /**
     * Вычисляет значение полинома в заданной точке x, используя барицентрическую формулу.
     * Это быстрее, чем стандартная формула Лагранжа для больших наборов точек.
     * 
     * Барицентрическая формула: L(x) = [Σ(i=0 до n-1) (w_i * y_i / (x - x_i))] / [Σ(i=0 до n-1) (w_i / (x - x_i))]
     * где w_i - барицентрические веса
     * 
     * @param x значение, в котором нужно вычислить полином
     * @return значение полинома в точке x
     */
    @Override
    public double evaluate(double x) {
        if (needsRebuild) {
            rebuildPolynomial();
        }
        
        // Особый случай для одинаковых значений y
        if (hasIdenticalYValues()) {
            return points.get(0).getY();
        }
        
        // Проверяем, является ли x одной из интерполяционных точек
        for (Point p : points) {
            if (Math.abs(x - p.getX()) < EPSILON) {
                return p.getY();
            }
        }
        
        // Барицентрическая формула для эффективного вычисления
        double numerator = 0.0;
        double denominator = 0.0;
        
        for (int i = 0; i < points.size(); i++) {
            double xi = points.get(i).getX();
            double yi = points.get(i).getY();
            
            double temp = barycentricWeights[i] / (x - xi);
            numerator += temp * yi;
            denominator += temp;
        }
        
        return numerator / denominator;
    }

    /**
     * Возвращает степень этого полинома, которая равна n-1 для n точек.
     * 
     * @return степень полинома
     */
    @Override
    public int degree() {
        return points.size() - 1;
    }

    /**
     * Возвращает коэффициенты этого полинома.
     * 
     * @return список коэффициентов от константного члена до члена наивысшей степени
     */
    @Override
    public List<Double> getCoefficients() {
        if (needsRebuild) {
            rebuildPolynomial();
        }
        
        return super.getCoefficients();
    }

    @Override
    public String toString() {
        if (needsRebuild) {
            rebuildPolynomial();
        }
        
        // Стандартное строковое представление с использованием коэффициентов
        int polyDegree = points.size() - 1;
        
        if (polyDegree == 0) {
            double coeff = getCoefficients().get(0);
            return String.format(Locale.US, "%.2f", coeff);
        }

        StringBuilder sb = new StringBuilder();
        List<Double> coeffs = getCoefficients();
        
        // Обрабатываем случай, когда все значения y одинаковы, получая константный полином
        if (coeffs.size() == 1) {
            return String.format(Locale.US, "%.2f", coeffs.get(0));
        }
        
        for (int i = polyDegree; i >= 0; i--) {
            double coeff = i < coeffs.size() ? coeffs.get(i) : 0.0;
            if (Math.abs(coeff) < EPSILON) continue;
            
            if (sb.length() > 0) {
                sb.append(coeff > 0 ? " + " : " - ");
            } else if (coeff < 0) {
                sb.append("-");
            }
            
            String coeffStr;
            coeffStr = String.format(Locale.US, "%.2f", Math.abs(coeff));

            if (i == 0) {
                sb.append(coeffStr);
            } else if (i == 1) {
                sb.append(Math.abs(coeff) == 1.0 ? "1.00x" : coeffStr + "x");
            } else {
                sb.append(Math.abs(coeff) == 1.0 ? "1.00x^" + i : coeffStr + "x^" + i);
            }
        }
        
        return sb.toString();
    }

    /**
     * Добавляет новую точку интерполяции. Полином будет автоматически перестроен
     * при следующем вычислении или запросе коэффициентов.
     * 
     * @param newPoint точка для добавления
     * @throws NullPointerException если newPoint равно null
     * @throws IllegalArgumentException если значение x дублируется
     */
    public void addPoint(Point newPoint) {
        Objects.requireNonNull(newPoint, "Point cannot be null");
        
        // Находим точку вставки для сохранения порядка сортировки
        int insertIndex = findInsertionPoint(newPoint.getX());
        
        // Проверка на дублирующиеся значения x
        if (insertIndex < points.size()) {
            double existingX = points.get(insertIndex).getX();
            if (Math.abs(existingX - newPoint.getX()) < EPSILON) {
                throw new IllegalArgumentException("Duplicate x value is not allowed: " + newPoint.getX());
            }
        }
        
        // Добавляем точку в правильную позицию для поддержания сортировки
        points.add(insertIndex, newPoint);
        computeBarycentricWeights();
        needsRebuild = true;
    }
    
    /**
     * Находит точку вставки для нового значения x, используя бинарный поиск.
     * 
     * @param x значение x для вставки
     * @return индекс, куда должна быть вставлена точка
     */
    private int findInsertionPoint(double x) {
        int low = 0;
        int high = points.size() - 1;
        
        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midX = points.get(mid).getX();
            
            if (Math.abs(midX - x) < EPSILON) {
                return mid; // Найден дубликат
            } else if (midX < x) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        
        return low; // Точка вставки
    }
}