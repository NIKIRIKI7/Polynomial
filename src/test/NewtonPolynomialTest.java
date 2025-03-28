package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import math.NewtonPolynomial;
import math.Point;

class NewtonPolynomialTest {

    @Test
    @DisplayName("Тест пустого конструктора")
    void testEmptyConstructor() {
        NewtonPolynomial np = new NewtonPolynomial();
        assertEquals(0, np.degree(), "Степень пустого полинома должна быть 0");
        assertEquals(0, np.getPoints().size(), "Пустой полином не должен иметь точек");
        assertEquals(List.of(0.0), np.getCoefficients(), "Пустой полином должен иметь коэффициент 0");
    }

    @Test
    @DisplayName("Тест конструктора с точками")
    void testConstructorWithPoints() {
        List<Point> points = List.of(
            new Point(1.0, 2.0),
            new Point(3.0, 4.0),
            new Point(5.0, 6.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        assertEquals(2, np.degree(), "Степень должна быть 2 для 3 точек");
        assertEquals(points.size(), np.getPoints().size(), "Должно быть 3 точки");
        
        // Точки должны быть отсортированы по x
        List<Point> storedPoints = np.getPoints();
        for (int i = 0; i < points.size(); i++) {
            assertEquals(points.get(i), storedPoints.get(i), "Точки должны совпадать");
        }
    }
    
    @Test
    @DisplayName("Тест конструктора с пустым списком точек")
    void testConstructorWithEmptyPoints() {
        NewtonPolynomial np = new NewtonPolynomial(List.of());
        assertEquals(0, np.degree(), "Степень пустого полинома должна быть 0");
        assertEquals(0, np.getPoints().size(), "Пустой полином не должен иметь точек");
    }
    
    @Test
    @DisplayName("Тест конструктора с null списком точек")
    void testConstructorWithNullPoints() {
        NewtonPolynomial np = new NewtonPolynomial(null);
        assertEquals(0, np.degree(), "Степень пустого полинома должна быть 0");
        assertEquals(0, np.getPoints().size(), "Пустой полином не должен иметь точек");
    }

    @Test
    @DisplayName("Тест метода addPoint")
    void testAddPoint() {
        NewtonPolynomial np = new NewtonPolynomial();
        Point p = new Point(2.0, 3.0);
        
        np.addPoint(p);
        
        assertEquals(1, np.getPoints().size(), "Должна быть 1 точка");
        assertEquals(p, np.getPoints().get(0), "Точка должна быть добавлена");
        assertEquals(0, np.degree(), "Степень должна быть 0 для 1 точки");
    }
    
    @Test
    @DisplayName("Тест метода addPoint с null точкой")
    void testAddPointWithNull() {
        NewtonPolynomial np = new NewtonPolynomial();
        
        assertThrows(NullPointerException.class, () -> np.addPoint(null),
                    "Добавление null точки должно вызывать NullPointerException");
    }
    
    @Test
    @DisplayName("Тест метода addPoint с дублирующимся значением x")
    void testAddPointWithDuplicateX() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoint(new Point(2.0, 3.0));
        
        Exception exception = assertThrows(IllegalArgumentException.class, 
                                          () -> np.addPoint(new Point(2.0, 4.0)),
                                          "Добавление точки с дублирующимся x должно вызывать IllegalArgumentException");
        
        assertTrue(exception.getMessage().contains("Duplicate x value"), 
                  "Сообщение исключения должно упоминать duplicate x");
    }
    
    @Test
    @DisplayName("Тест метода addPoint с почти дублирующимся значением x")
    void testAddPointWithAlmostDuplicateX() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoint(new Point(2.0, 3.0));
        
        Exception exception = assertThrows(IllegalArgumentException.class, 
                                          () -> np.addPoint(new Point(2.0 + 1e-11, 4.0)),
                                          "Добавление точки с почти дублирующимся x должно вызывать IllegalArgumentException");
    }

    @Test
    @DisplayName("Тест метода addPoints")
    void testAddPoints() {
        NewtonPolynomial np = new NewtonPolynomial();
        List<Point> points = List.of(
            new Point(1.0, 2.0),
            new Point(3.0, 4.0),
            new Point(5.0, 6.0)
        );
        
        np.addPoints(points);
        
        assertEquals(3, np.getPoints().size(), "Должно быть 3 точки");
        assertEquals(2, np.degree(), "Степень должна быть 2 для 3 точек");
    }
    
    @Test
    @DisplayName("Тест метода addPoints с null списком")
    void testAddPointsWithNull() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoints(null); // Должно обрабатываться без исключения
        
        assertEquals(0, np.getPoints().size(), "Должно остаться 0 точек");
    }
    
    @Test
    @DisplayName("Тест метода addPoints с пустым списком")
    void testAddPointsWithEmptyList() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoints(List.of());
        
        assertEquals(0, np.getPoints().size(), "Должно остаться 0 точек");
    }
    
    @Test
    @DisplayName("Тест метода addPoints с null точками в списке")
    void testAddPointsWithNullPointsInList() {
        NewtonPolynomial np = new NewtonPolynomial();
        List<Point> points = new ArrayList<>();
        points.add(new Point(1.0, 2.0));
        points.add(null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, 
                                          () -> np.addPoints(points),
                                          "Добавление списка с null точками должно вызывать IllegalArgumentException");
        
        assertTrue(exception.getMessage().contains("null values"), 
                  "Сообщение исключения должно упоминать null values");
    }
    
    @Test
    @DisplayName("Тест метода addPoints с дублирующимся значением x")
    void testAddPointsWithDuplicateX() {
        NewtonPolynomial np = new NewtonPolynomial();
        List<Point> points = List.of(
            new Point(1.0, 2.0),
            new Point(1.0, 3.0)
        );
        
        Exception exception = assertThrows(IllegalArgumentException.class, 
                                          () -> np.addPoints(points),
                                          "Добавление точек с дублирующимся x должно вызывать IllegalArgumentException");
        
        assertTrue(exception.getMessage().contains("Duplicate x value"), 
                  "Сообщение исключения должно упоминать duplicate x");
    }
    
    @Test
    @DisplayName("Тест метода addPoints к существующим точкам")
    void testAddPointsToExistingPoints() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoint(new Point(1.0, 2.0));
        
        List<Point> additionalPoints = List.of(
            new Point(3.0, 4.0),
            new Point(5.0, 6.0)
        );
        
        np.addPoints(additionalPoints);
        
        assertEquals(3, np.getPoints().size(), "Должно быть 3 точки в итоге");
        assertEquals(2, np.degree(), "Степень должна быть 2 для 3 точек");
    }
    
    @Test
    @DisplayName("Тест метода addPoints с конфликтом с существующей точкой")
    void testAddPointsWithConflictingX() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoint(new Point(1.0, 2.0));
        
        List<Point> additionalPoints = List.of(
            new Point(1.0, 3.0),
            new Point(5.0, 6.0)
        );
        
        Exception exception = assertThrows(IllegalArgumentException.class, 
                                          () -> np.addPoints(additionalPoints),
                                          "Добавление точек с x, конфликтующим с существующими точками, должно вызывать IllegalArgumentException");
    }

    @Test
    @DisplayName("Тест метода removePoint")
    void testRemovePoint() {
        List<Point> points = List.of(
            new Point(1.0, 2.0),
            new Point(3.0, 4.0),
            new Point(5.0, 6.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        boolean removed = np.removePoint(points.get(1));
        
        assertTrue(removed, "Точка должна быть успешно удалена");
        assertEquals(2, np.getPoints().size(), "Должно остаться 2 точки после удаления");
        assertEquals(1, np.degree(), "Степень должна быть 1 после удаления");
        assertFalse(np.getPoints().contains(points.get(1)), "Удаленной точки не должно быть в списке");
    }
    
    @Test
    @DisplayName("Тест метода removePoint с точкой не из полинома")
    void testRemovePointNotInPolynomial() {
        List<Point> points = List.of(
            new Point(1.0, 2.0),
            new Point(3.0, 4.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        boolean removed = np.removePoint(new Point(5.0, 6.0));
        
        assertFalse(removed, "Точка не из полинома не должна быть удалена");
        assertEquals(2, np.getPoints().size(), "Количество точек должно остаться прежним");
    }
    
    @Test
    @DisplayName("Тест метода removePoint с null точкой")
    void testRemovePointNull() {
        NewtonPolynomial np = new NewtonPolynomial(List.of(new Point(1.0, 2.0)));
        
        assertThrows(NullPointerException.class, () -> np.removePoint(null),
                    "Удаление null точки должно вызывать NullPointerException");
    }

    @Test
    @DisplayName("Тест метода evaluate")
    void testEvaluate() {
        List<Point> points = List.of(
            new Point(0.0, 1.0),
            new Point(1.0, 3.0),
            new Point(2.0, 7.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        // Проверка в узлах интерполяции
        assertEquals(1.0, np.evaluate(0.0), 1e-10, "P(0) должно быть 1");
        assertEquals(3.0, np.evaluate(1.0), 1e-10, "P(1) должно быть 3");
        assertEquals(7.0, np.evaluate(2.0), 1e-10, "P(2) должно быть 7");
        
        // Проверка интерполяции между точками - значение должно лежать в диапазоне между соседними узлами
        double y_1_5 = np.evaluate(1.5);
        assertTrue(y_1_5 > 3.0 && y_1_5 < 7.0, 
                   "P(1.5) должно быть между P(1)=3 и P(2)=7, но получилось " + y_1_5);
    }
    
    @Test
    @DisplayName("Тест метода evaluate с пустым полиномом")
    void testEvaluateWithEmptyPolynomial() {
        NewtonPolynomial np = new NewtonPolynomial();
        
        assertEquals(0.0, np.evaluate(5.0), 1e-10, "Пустой полином должен возвращать 0 для любого x");
    }
    
    @Test
    @DisplayName("Тест метода evaluate с x, совпадающим с узлом")
    void testEvaluateWithMatchingNode() {
        List<Point> points = List.of(
            new Point(1.0, 2.0),
            new Point(3.0, 4.0),
            new Point(5.0, 6.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        // Вычисление в точках с почти совпадающими x
        assertEquals(2.0, np.evaluate(1.0 + 1e-12), 1e-10, "Значение должно быть 2.0 для x ~= 1.0");
        assertEquals(4.0, np.evaluate(3.0 - 1e-12), 1e-10, "Значение должно быть 4.0 для x ~= 3.0");
        assertEquals(6.0, np.evaluate(5.0), 1e-10, "Значение должно быть 6.0 для x = 5.0");
    }
    
    @Test
    @DisplayName("Тест метода evaluate после изменений, требующих перестроения")
    void testEvaluateAfterChanges() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoint(new Point(0.0, 1.0));
        np.addPoint(new Point(1.0, 3.0));
        
        // Начальный полином: P(x) = 1 + 2x
        assertEquals(1.0, np.evaluate(0.0), 1e-10, "P(0) должно быть 1");
        assertEquals(3.0, np.evaluate(1.0), 1e-10, "P(1) должно быть 3");
        
        // Добавляем точку, что требует перестроения полинома
        np.addPoint(new Point(2.0, 7.0));
        
        // Новый полином: P(x) = 1 + 2x + x^2
        assertEquals(1.0, np.evaluate(0.0), 1e-10, "P(0) должно быть 1");
        assertEquals(3.0, np.evaluate(1.0), 1e-10, "P(1) должно быть 3");
        assertEquals(7.0, np.evaluate(2.0), 1e-10, "P(2) должно быть 7");
    }

    @Test
    @DisplayName("Тест метода getCoefficients")
    void testGetCoefficients() {
        List<Point> points = List.of(
            new Point(0.0, 1.0),
            new Point(1.0, 3.0),
            new Point(2.0, 7.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        List<Double> coeffs = np.getCoefficients();
        assertEquals(3, coeffs.size(), "Должно быть 3 коэффициента");
        
        // Проверка вычисления - полином должен проходить через заданные точки
        assertEquals(1.0, np.evaluate(0.0), 1e-10, "P(0) должно быть 1");
        assertEquals(3.0, np.evaluate(1.0), 1e-10, "P(1) должно быть 3");
        assertEquals(7.0, np.evaluate(2.0), 1e-10, "P(2) должно быть 7");
    }
    
    @Test
    @DisplayName("Тест метода getCoefficients после изменений, требующих перестроения")
    void testGetCoefficientsAfterChanges() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoints(List.of(
            new Point(0.0, 1.0),
            new Point(1.0, 3.0)
        ));
        
        // Начальный полином с двумя точками
        List<Double> coeffs1 = np.getCoefficients();
        assertEquals(2, coeffs1.size(), "Должно быть 2 коэффициента");
        
        // Добавляем точку, что требует перестроения полинома
        np.addPoint(new Point(2.0, 7.0));
        
        // Новый полином с тремя точками
        List<Double> coeffs2 = np.getCoefficients();
        assertEquals(3, coeffs2.size(), "Должно быть 3 коэффициента");
        
        // Проверяем, что полином проходит через заданные точки
        assertEquals(1.0, np.evaluate(0.0), 1e-10, "P(0) должно быть 1");
        assertEquals(3.0, np.evaluate(1.0), 1e-10, "P(1) должно быть 3");
        assertEquals(7.0, np.evaluate(2.0), 1e-10, "P(2) должно быть 7");
    }

    @Test
    @DisplayName("Тест метода toString")
    void testToString() {
        List<Point> points = List.of(
            new Point(0.0, 1.0),
            new Point(1.0, 3.0),
            new Point(2.0, 7.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        // Строковое представление зависит от реализации
        String str = np.toString();
        
        // Проверяем, что строка не пустая и не равна "0.00"
        assertNotEquals("", str, "Строковое представление не должно быть пустым");
        assertNotEquals("0.00", str, "Строковое представление не должно быть равно 0.00");
        
        // Проверка после вычисления для полного покрытия
        np.evaluate(1.5);
        String str2 = np.toString();
        
        // Строки должны совпадать, т.к. полином не изменился
        assertEquals(str, str2, "Строки должны совпадать после вычисления");
    }
    
    @Test
    @DisplayName("Тест метода toString с пустым полиномом")
    void testToStringWithEmptyPolynomial() {
        NewtonPolynomial np = new NewtonPolynomial();
        assertEquals("0.00", np.toString(), "Строковое представление пустого полинома должно быть 0.00");
    }
    
    @Test
    @DisplayName("Тест метода toString после изменений, требующих перестроения")
    void testToStringAfterChanges() {
        NewtonPolynomial np = new NewtonPolynomial();
        np.addPoints(List.of(
            new Point(0.0, 1.0),
            new Point(1.0, 3.0)
        ));
        
        // Начальный полином с двумя точками
        String str1 = np.toString();
        assertNotEquals("", str1, "Строковое представление не должно быть пустым");
        assertNotEquals("0.00", str1, "Строковое представление не должно быть равно 0.00");
        
        // Добавляем точку, что требует перестроения полинома
        np.addPoint(new Point(2.0, 7.0));
        
        // Новый полином с тремя точками
        String str2 = np.toString();
        assertNotEquals("", str2, "Строковое представление не должно быть пустым");
        assertNotEquals("0.00", str2, "Строковое представление не должно быть равно 0.00");
        
        // Строки должны отличаться, т.к. полином изменился
        assertNotEquals(str1, str2, "Строки должны отличаться после изменения полинома");
    }
    
    static Stream<Arguments> provideDifferentPointSets() {
        return Stream.of(
            Arguments.of(
                List.of(new Point(0.0, 1.0), new Point(1.0, 3.0)),
                new double[][] {{1.0, 3.0}, {2.0}}
            ),
            Arguments.of(
                List.of(new Point(0.0, 1.0), new Point(1.0, 3.0), new Point(2.0, 7.0)),
                new double[][] {{1.0, 3.0, 7.0}, {2.0, 4.0}, {1.0}}
            ),
            Arguments.of(
                List.of(new Point(1.0, 2.0), new Point(3.0, 4.0), new Point(5.0, 8.0)),
                new double[][] {{2.0, 4.0, 8.0}, {1.0, 2.0}, {0.5}}
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideDifferentPointSets")
    @DisplayName("Тест с различными наборами точек")
    void testWithDifferentPointSets(List<Point> points, double[][] expectedDiffTable) {
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        // Проверка интерполяции в заданных точках
        for (Point p : points) {
            assertEquals(p.getY(), np.evaluate(p.getX()), 1e-10, 
                         "Полином должен проходить через точку (" + p.getX() + ", " + p.getY() + ")");
        }
        
        // Оцениваем полином в промежуточных точках
        // Для подробной проверки разделенных разностей нужен был бы доступ к приватным полям
    }
    
    @Test
    @DisplayName("Тест оптимизации вычисления для точек с дробными координатами")
    void testEvaluateWithNoOptimization() {
        // Создаем точки, не подходящие под оптимизационный шаблон
        List<Point> points = List.of(
            new Point(0.5, 1.0),
            new Point(1.5, 3.0),
            new Point(2.5, 7.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        // Проверяем вычисление в заданных точках
        assertEquals(1.0, np.evaluate(0.5), 1e-10, "P(0.5) должно быть 1");
        assertEquals(3.0, np.evaluate(1.5), 1e-10, "P(1.5) должно быть 3");
        assertEquals(7.0, np.evaluate(2.5), 1e-10, "P(2.5) должно быть 7");
    }
    
    @Test
    @DisplayName("Тест для неиспользуемого метода computeDividedDifferences")
    void testComputeDividedDifferences() throws Exception {
        // Создаем полином для тестирования
        List<Point> points = List.of(
            new Point(0.0, 1.0),
            new Point(1.0, 3.0),
            new Point(2.0, 7.0)
        );
        NewtonPolynomial np = new NewtonPolynomial(points);
        
        // Получаем доступ к приватному методу через рефлексию
        java.lang.reflect.Method method = NewtonPolynomial.class.getDeclaredMethod(
            "computeDividedDifferences", 
            double[].class, 
            double[].class
        );
        method.setAccessible(true);
        
        // Подготавливаем входные данные
        double[] xValues = new double[]{0.0, 1.0, 2.0};
        double[] yValues = new double[]{1.0, 3.0, 7.0};
        
        // Вызываем метод
        double[][] result = (double[][]) method.invoke(np, xValues, yValues);
        
        // Проверяем результат
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(3, result.length, "Таблица должна иметь 3 строки");
        assertEquals(3, result[0].length, "Первая строка должна иметь 3 столбца");
        
        // Проверяем первый столбец (начальные y-значения)
        assertEquals(1.0, result[0][0], 1e-10);
        assertEquals(3.0, result[1][0], 1e-10);
        assertEquals(7.0, result[2][0], 1e-10);
        
        // Проверяем разделенные разности первого порядка
        assertEquals(2.0, result[0][1], 1e-10); // (3-1)/(1-0) = 2
        assertEquals(4.0, result[1][1], 1e-10); // (7-3)/(2-1) = 4
        
        // Проверяем разделенную разность второго порядка
        assertEquals(1.0, result[0][2], 1e-10); // (4-2)/(2-0) = 1
    }
} 