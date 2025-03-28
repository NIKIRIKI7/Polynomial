package math;

import java.util.*;

public class Polynomial {
    protected static final double EPSILON = 1e-10; // Константа для сравнения чисел с плавающей точкой
    private List<Double> coefficients;

    /**
     * Создает пустой полином (равный нулю).
     * Полином представляется в виде: p(x) = a₀ + a₁x + a₂x² + ... + aₙxⁿ,
     * где a₀, a₁, ..., aₙ - коэффициенты полинома.
     */
    public Polynomial() {
        coefficients = new ArrayList<>(List.of(0.0));
    }

    /**
     * Создает полином из списка коэффициентов.
     * Коэффициенты указываются от младшего к старшему: [a₀, a₁, a₂, ..., aₙ]
     * 
     * @param coefficients список коэффициентов полинома
     */
    public Polynomial(List<Double> coefficients) {
        this.coefficients = new ArrayList<>(Objects.requireNonNull(coefficients));
        trim();
        if (this.coefficients.isEmpty()) {
            this.coefficients.add(0.0);
        }
    }

    /**
     * Обновляет коэффициенты полинома.
     * 
     * @param newCoefficients новый список коэффициентов полинома
     */
    protected void updateCoefficients(List<Double> newCoefficients) {
        this.coefficients = new ArrayList<>(Objects.requireNonNull(newCoefficients));
        trim();
        if (this.coefficients.isEmpty()) {
            this.coefficients.add(0.0);
        }
    }

    /**
     * Создает полином из массива коэффициентов.
     * Коэффициенты указываются от младшего к старшему: [a₀, a₁, a₂, ..., aₙ]
     * Оптимизирован для избежания промежуточного boxing/unboxing.
     * 
     * @param coefficients массив коэффициентов полинома
     */
    public Polynomial(double... coefficients) {
        this.coefficients = new ArrayList<>(coefficients.length);
        for (double coeff : coefficients) {
            this.coefficients.add(coeff);
        }
        trim();
        if (this.coefficients.isEmpty()) {
            this.coefficients.add(0.0);
        }
    }

    /**
     * Удаляет старшие нулевые коэффициенты полинома.
     * Это позволяет корректно определить степень полинома и
     * оптимизировать память.
     */
    private void trim() {
        int lastNonZeroIndex = coefficients.size() - 1;
        while (lastNonZeroIndex > 0 && Math.abs(coefficients.get(lastNonZeroIndex)) < EPSILON) {
            lastNonZeroIndex--;
        }
        if (lastNonZeroIndex < coefficients.size() - 1) {
            coefficients.subList(lastNonZeroIndex + 1, coefficients.size()).clear();
        }
    }

    /**
     * Возвращает степень полинома.
     * Степень полинома - это наивысшая степень x с ненулевым коэффициентом.
     * 
     * @return степень полинома
     */
    public int degree() {
        return coefficients.size() - 1;
    }

    /**
     * Возвращает список коэффициентов полинома.
     * 
     * @return список коэффициентов от a₀ до aₙ
     */
    public List<Double> getCoefficients() {
        return new ArrayList<>(coefficients);
    }

    /**
     * Возвращает старший коэффициент полинома (при xⁿ).
     * 
     * @return старший коэффициент полинома
     */
    private double lastCoefficient() {
        int deg = degree();
        return deg >= 0 ? coefficients.get(deg) : 0.0;
    }

    /**
     * Возвращает строковое представление полинома в виде:
     * aₙxⁿ + aₙ₋₁xⁿ⁻¹ + ... + a₁x + a₀
     * 
     * @return строковое представление полинома
     */
    @Override
    public String toString() {
        if (degree() == 0) {
            return formatCoefficient(coefficients.get(0));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = degree(); i >= 0; i--) {
            double coeff = coefficients.get(i);
            if (Math.abs(coeff) < EPSILON) continue; // Используем погрешность вместо точного сравнения с 0

            // Добавляем знак перед термом (кроме первого положительного)
            if (sb.length() > 0) {
                sb.append(coeff > 0 ? " + " : " - ");
            } else if (coeff < 0) {
                sb.append("-");
            }

            String term = formatTerm(Math.abs(coeff), i);
            sb.append(term);
        }
        return sb.toString();
    }

    /**
     * Форматирует один член полинома.
     * 
     * @param coefficient коэффициент
     * @param exponent показатель степени
     * @return отформатированный член полинома
     */
    private String formatTerm(double coefficient, int exponent) {
        String coeffStr = formatCoefficient(coefficient);
        if (exponent == 0) return coeffStr;
        if (exponent == 1) return coeffStr.equals("1.00") ? "1.00x" : coeffStr + "x";
        return coeffStr.equals("1.00") ? "1.00x^" + exponent : coeffStr + "x^" + exponent;
    }
    
    /**
     * Форматирует коэффициент полинома.
     * 
     * @param coefficient коэффициент для форматирования
     * @return отформатированный коэффициент
     */
    private String formatCoefficient(double coefficient) {
        return String.format(Locale.US, "%.2f", coefficient);
    }

    /**
     * Сравнивает два полинома на равенство.
     * Полиномы считаются равными, если их степени равны и
     * соответствующие коэффициенты равны с точностью до EPSILON.
     * 
     * @param obj объект для сравнения
     * @return true если полиномы равны, иначе false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Проверка идентичности
        if (!(obj instanceof Polynomial)) return false; // Проверка типа
        
        Polynomial other = (Polynomial) obj;
        if (degree() != other.degree()) return false;
        
        for (int i = 0; i <= degree(); i++) {
            if (Math.abs(coefficients.get(i) - other.coefficients.get(i)) > EPSILON) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coefficients);
    }

    /**
     * Складывает два полинома.
     * Операция: p(x) + q(x) = (a₀ + b₀) + (a₁ + b₁)x + (a₂ + b₂)x² + ...
     * Оптимизировано для уменьшения количества операций.
     * 
     * @param other полином для сложения
     * @return новый полином, являющийся суммой
     */
    public Polynomial add(Polynomial other) {
        int maxDegree = Math.max(degree(), other.degree());
        double[] result = new double[maxDegree + 1];

        // Копируем коэффициенты из первого полинома
        for (int i = 0; i <= degree(); i++) {
            result[i] = coefficients.get(i);
        }
        
        // Добавляем коэффициенты из второго полинома
        for (int i = 0; i <= other.degree(); i++) {
            result[i] += other.coefficients.get(i);
        }

        // Создаем новый полином напрямую из массива double[]
        return new Polynomial(result);
    }

    /**
     * Вычитает полином из текущего.
     * Операция: p(x) - q(x) = (a₀ - b₀) + (a₁ - b₁)x + (a₂ - b₂)x² + ...
     * Оптимизировано для уменьшения количества операций.
     * 
     * @param other полином для вычитания
     * @return новый полином, являющийся разностью
     */
    public Polynomial subtract(Polynomial other) {
        int maxDegree = Math.max(degree(), other.degree());
        double[] result = new double[maxDegree + 1];

        // Копируем коэффициенты из первого полинома
        for (int i = 0; i <= degree(); i++) {
            result[i] = coefficients.get(i);
        }
        
        // Вычитаем коэффициенты из второго полинома
        for (int i = 0; i <= other.degree(); i++) {
            result[i] -= other.coefficients.get(i);
        }

        return new Polynomial(result);
    }

    /**
     * Умножает два полинома.
     * Операция: p(x) * q(x) = Σ(i=0 до n) Σ(j=0 до m) a_i * b_j * x^(i+j)
     * Оптимизировано с использованием прямого массива.
     * 
     * @param other полином для умножения
     * @return новый полином, являющийся произведением
     */
    public Polynomial multiply(Polynomial other) {
        int n = degree();
        int m = other.degree();
        int resultDegree = n + m;
        double[] result = new double[resultDegree + 1];

        for (int i = 0; i <= n; i++) {
            double a = coefficients.get(i);
            if (Math.abs(a) < EPSILON) continue; // Пропускаем почти нулевые коэффициенты
            
            for (int j = 0; j <= m; j++) {
                double b = other.coefficients.get(j);
                if (Math.abs(b) < EPSILON) continue; // Пропускаем почти нулевые коэффициенты
                
                result[i + j] += a * b;
            }
        }

        return new Polynomial(result);
    }

    /**
     * Умножает полином на скаляр.
     * Операция: c * p(x) = c*a₀ + c*a₁x + c*a₂x² + ...
     * Оптимизировано для уменьшения количества операций.
     * 
     * @param scalar скаляр для умножения
     * @return новый полином, умноженный на скаляр
     */
    public Polynomial multiply(double scalar) {
        if (Math.abs(scalar) < EPSILON) {
            return new Polynomial(); // Возвращаем нулевой полином
        }
        
        double[] result = new double[coefficients.size()];
        for (int i = 0; i < coefficients.size(); i++) {
            result[i] = coefficients.get(i) * scalar;
        }
        return new Polynomial(result);
    }

    /**
     * Делит полином на скаляр.
     * Операция: p(x)/c = (a₀/c) + (a₁/c)x + (a₂/c)x² + ...
     * Оптимизировано для уменьшения количества операций.
     * 
     * @param scalar скаляр для деления
     * @return новый полином, поделенный на скаляр
     * @throws IllegalArgumentException если скаляр равен нулю
     */
    public Polynomial divide(double scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new IllegalArgumentException("Division by zero");
        }
        
        double[] result = new double[coefficients.size()];
        for (int i = 0; i < coefficients.size(); i++) {
            result[i] = coefficients.get(i) / scalar;
        }
        return new Polynomial(result);
    }

    /**
     * Вычисляет значение полинома в точке x.
     * Использует схему Горнера: p(x) = a₀ + x(a₁ + x(a₂ + ... + x(aₙ₋₁ + x*aₙ)...))
     * Сложность: O(n), где n - степень полинома.
     * 
     * @param x точка, в которой вычисляется значение полинома
     * @return значение полинома p(x)
     */
    public double evaluate(double x) {
        if (degree() == 0) return coefficients.get(0);
        if (Math.abs(x) < EPSILON) return coefficients.get(0); // Оптимизация для x=0

        
        double result = coefficients.get(degree());
        for (int i = degree() - 1; i >= 0; i--) {
            result = result * x + coefficients.get(i);
        }
        return result;
    }

    // Utility methods for polynomial subclasses
    
    /**
     * Проверяет наличие дублирующихся значений x в отсортированном списке точек.
     * 
     * @param points отсортированный список точек для проверки
     * @throws IllegalArgumentException если найдены дублирующиеся значения x
     */
    protected static void checkForDuplicateXAfterSort(List<Point> points) {
        for (int i = 1; i < points.size(); i++) {
            if (Math.abs(points.get(i).getX() - points.get(i-1).getX()) < EPSILON) {
                throw new IllegalArgumentException("Duplicate x values are not allowed: " + points.get(i).getX());
            }
        }
    }
    
    /**
     * Сортирует точки по координате x.
     * 
     * @param points список точек для сортировки
     * @return новый отсортированный список точек
     */
    protected static List<Point> sortPointsByX(List<Point> points) {
        List<Point> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingDouble(Point::getX));
        return sorted;
    }
}