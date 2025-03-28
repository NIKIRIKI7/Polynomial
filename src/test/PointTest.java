package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Locale;

import math.Point;

public class PointTest {

    @Test
    @DisplayName("Тест конструктора Point и геттеров")
    void testConstructorAndGetters() {
        Point point = new Point(5.0, 10.0);
        assertEquals(5.0, point.getX(), "X координата должна быть 5.0");
        assertEquals(10.0, point.getY(), "Y координата должна быть 10.0");
    }

    @Test
    @DisplayName("Тест метода equals с идентичными точками")
    void testEqualsWithIdenticalPoints() {
        Point point1 = new Point(3.0, 4.0);
        Point point2 = new Point(3.0, 4.0);
        assertEquals(point1, point2, "Идентичные точки должны быть равны");
    }

    @Test
    @DisplayName("Тест метода equals при сравнении с самим собой")
    void testEqualsWithSameObject() {
        Point point = new Point(3.0, 4.0);
        assertTrue(point.equals(point), "Точка должна быть равна сама себе");
    }

    @Test
    @DisplayName("Тест метода equals с разными точками")
    void testEqualsWithDifferentPoints() {
        Point point1 = new Point(3.0, 4.0);
        Point point2 = new Point(3.0, 5.0);
        Point point3 = new Point(4.0, 4.0);
        
        assertNotEquals(point1, point2, "Точки с разными значениями y не должны быть равны");
        assertNotEquals(point1, point3, "Точки с разными значениями x не должны быть равны");
    }

    @Test
    @DisplayName("Тест метода equals с null и другими типами")
    void testEqualsWithNullAndDifferentTypes() {
        Point point = new Point(3.0, 4.0);
        assertNotEquals(null, point, "Точка не должна быть равна null");
        assertNotEquals("not a point", point, "Точка не должна быть равна объекту другого типа");
    }

    @Test
    @DisplayName("Тест метода equals с очень близкими значениями (сравнение с эпсилон)")
    void testEqualsWithVeryCloseValues() {
        Point point1 = new Point(3.0, 4.0);
        Point point2 = new Point(3.0 + 1e-11, 4.0 - 1e-11);
        assertEquals(point1, point2, "Точки с очень близкими значениями должны быть равны (в пределах эпсилон)");
        
        Point point3 = new Point(3.0 + 1e-9, 4.0);
        assertNotEquals(point1, point3, "Точки со значениями за пределами эпсилон не должны быть равны");
    }

    @Test
    @DisplayName("Тест согласованности hashCode")
    void testHashCodeConsistency() {
        Point point1 = new Point(3.0, 4.0);
        Point point2 = new Point(3.0, 4.0);
        
        assertEquals(point1.hashCode(), point2.hashCode(), "Равные точки должны иметь одинаковые хэш-коды");
        
        // Проверка того, что hashCode возвращает одно и то же значение
        int hash1 = point1.hashCode();
        int hash2 = point1.hashCode();
        assertEquals(hash1, hash2, "Хэш-код должен быть согласованным при многократных вызовах");
    }

    @Test
    @DisplayName("Тест метода toString")
    void testToString() {
        Point point = new Point(3.5, 4.7);
        String expected = "(3.50, 4.70)";
        assertEquals(expected, point.toString(), "toString должен возвращать отформатированные координаты");
    }

    @ParameterizedTest
    @CsvSource({
        "0.0, 0.0",
        "1.0, 1.0",
        "-5.5, 10.2",
        "1234.56, -7890.12"
    })
    @DisplayName("Тест метода toString с различными входными данными")
    void testToStringParameterized(double x, double y) {
        Point point = new Point(x, y);
        String expected = String.format(Locale.US, "(%.2f, %.2f)", x, y);
        assertEquals(expected, point.toString(), "toString должен правильно форматировать координаты");
    }
} 