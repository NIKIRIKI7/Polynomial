package math;

import java.util.Objects;
import java.util.Locale;

/**
 * Неизменяемый класс, представляющий точку на плоскости с координатами (x, y).
 * Класс оптимизирован для эффективной работы с полиномами интерполяции.
 */
public final class Point {
    private final double x;
    private final double y;
    private final int hashCode; // Предварительно вычисленный хэш-код

    /**
     * Создает точку с указанными координатами.
     * 
     * @param x координата x
     * @param y координата y
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.hashCode = computeHashCode();
    }

    /**
     * Возвращает координату x.
     * 
     * @return координата x
     */
    public double getX() {
        return x;
    }

    /**
     * Возвращает координату y.
     * 
     * @return координата y
     */
    public double getY() {
        return y;
    }

    /**
     * Вычисляет хэш-код для точки.
     * Предварительное вычисление хэш-кода повышает производительность
     * при использовании точек в хэш-таблицах.
     * 
     * @return хэш-код
     */
    private int computeHashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Сравнивает эту точку с другим объектом.
     * Точки считаются равными, если их координаты равны с точностью до EPSILON.
     * 
     * @param o объект для сравнения
     * @return true если точки равны, иначе false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        
        // Используем константное значение для сравнения чисел с плавающей точкой
        return Math.abs(point.x - x) < Polynomial.EPSILON && Math.abs(point.y - y) < Polynomial.EPSILON;
    }

    /**
     * Возвращает хэш-код точки.
     * Используется предварительно вычисленный хэш-код для повышения производительности.
     * 
     * @return хэш-код точки
     */
    @Override
    public int hashCode() {
        return hashCode; // Возвращаем предварительно вычисленный хэш-код
    }

    /**
     * Возвращает строковое представление точки в формате (x, y).
     * 
     * @return строковое представление точки
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "(%.2f, %.2f)", x, y);
    }
}