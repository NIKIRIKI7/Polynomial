package test;

import math.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

import math.Polynomial;

class PolynomialTest {
    // Локальная константа для тестов, значение совпадает с константой в Polynomial
    private static final double EPSILON = 1e-10;

    @Test
    @DisplayName("Тест конструктора по умолчанию")
    void testDefaultConstructor() {
        Polynomial p = new Polynomial();
        assertEquals(0, p.degree(), "Полином по умолчанию должен иметь степень 0");
        assertEquals(List.of(0.0), p.getCoefficients(), "Полином по умолчанию должен иметь коэффициент 0");
    }

    @Test
    @DisplayName("Тест конструктора со списком коэффициентов")
    void testConstructorWithCoefficients() {
        List<Double> coeffs = Arrays.asList(1.0, 2.0, 3.0);
        Polynomial p = new Polynomial(coeffs);
        assertEquals(2, p.degree(), "Степень полинома должна быть 2");
        assertEquals(coeffs, p.getCoefficients(), "Коэффициенты должны совпадать");
    }

    @Test
    @DisplayName("Тест конструктора с пустым списком коэффициентов")
    void testConstructorWithEmptyCoefficients() {
        Polynomial p = new Polynomial(List.of());
        assertEquals(0, p.degree(), "Пустой полином должен иметь степень 0");
        assertEquals(List.of(0.0), p.getCoefficients(), "Пустой полином должен иметь коэффициент 0");
    }

    @Test
    @DisplayName("Тест конструктора с varargs")
    void testConstructorWithVarargs() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0);
        assertEquals(2, p.degree(), "Степень полинома должна быть 2");
        assertEquals(Arrays.asList(1.0, 2.0, 3.0), p.getCoefficients(), "Коэффициенты должны совпадать");
    }

    @Test
    @DisplayName("Тест конструктора с ведущими нулями")
    void testConstructorWithLeadingZeros() {
        Polynomial p = new Polynomial(1.0, 2.0, 0.0, 0.0);
        assertEquals(1, p.degree(), "Степень полинома должна быть 1 после обрезки");
        assertEquals(Arrays.asList(1.0, 2.0), p.getCoefficients(), "Коэффициенты должны быть обрезаны");
    }

    @Test
    @DisplayName("Тест метода updateCoefficients")
    void testUpdateCoefficients() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0);
        List<Double> newCoeffs = Arrays.asList(4.0, 5.0);
        
        // Используем рефлексию для доступа к защищённому методу
        try {
            java.lang.reflect.Method method = Polynomial.class.getDeclaredMethod(
                "updateCoefficients", List.class);
            method.setAccessible(true);
            method.invoke(p, newCoeffs);
            
            assertEquals(1, p.degree(), "Степень полинома должна быть обновлена");
            assertEquals(newCoeffs, p.getCoefficients(), "Коэффициенты должны быть обновлены");
        } catch (Exception e) {
            fail("Не удалось получить доступ к методу updateCoefficients: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Тест updateCoefficients с пустым списком")
    void testUpdateCoefficientsWithEmptyList() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0);
        
        // Используем рефлексию для доступа к защищённому методу
        try {
            java.lang.reflect.Method method = Polynomial.class.getDeclaredMethod(
                "updateCoefficients", List.class);
            method.setAccessible(true);
            method.invoke(p, List.of());
            
            assertEquals(0, p.degree(), "Степень полинома должна быть 0");
            assertEquals(List.of(0.0), p.getCoefficients(), "Коэффициенты должны быть [0.0]");
        } catch (Exception e) {
            fail("Не удалось получить доступ к методу updateCoefficients: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Тест метода toString")
    void testToString() {
        Polynomial p1 = new Polynomial(3.0, 2.0, 1.0);
        assertEquals("1.00x^2 + 2.00x + 3.00", p1.toString(), "toString должен отображать отформатированный полином");
        
        Polynomial p2 = new Polynomial(0.0);
        assertEquals("0.00", p2.toString(), "toString нулевого полинома должен быть '0.00'");
        
        Polynomial p3 = new Polynomial(-1.0, 2.0, -3.0);
        assertEquals("-3.00x^2 + 2.00x - 1.00", p3.toString(), "toString должен обрабатывать отрицательные коэффициенты");
        
        Polynomial p4 = new Polynomial(0.0, 1.0, 0.0, 1.0);
        assertEquals("1.00x^3 + 1.00x", p4.toString(), "toString должен пропускать нулевые коэффициенты");
    }

    @Test
    @DisplayName("Тест метода equals")
    void testEquals() {
        Polynomial p1 = new Polynomial(1.0, 2.0, 3.0);
        Polynomial p2 = new Polynomial(1.0, 2.0, 3.0);
        Polynomial p3 = new Polynomial(1.0, 2.0, 3.1);
        Polynomial p4 = new Polynomial(1.0, 2.0);
        
        assertEquals(p1, p1, "Полином должен быть равен самому себе");
        assertEquals(p1, p2, "Одинаковые полиномы должны быть равны");
        assertNotEquals(p1, p3, "Полиномы с разными коэффициентами не должны быть равны");
        assertNotEquals(p1, p4, "Полиномы разных степеней не должны быть равны");
        assertNotEquals(p1, null, "Полином не должен быть равен null");
        assertNotEquals(p1, "not a polynomial", "Полином не должен быть равен объекту другого типа");
    }
    
    @Test
    @DisplayName("Тест метода equals с эпсилоном")
    void testEqualsWithEpsilon() {
        Polynomial p1 = new Polynomial(1.0, 2.0, 3.0);
        Polynomial p2 = new Polynomial(1.0 + 1e-11, 2.0, 3.0 - 1e-11);
        Polynomial p3 = new Polynomial(1.0 + 1e-9, 2.0, 3.0);
        
        assertEquals(p1, p2, "Полиномы с очень близкими коэффициентами должны быть равны (в пределах эпсилон)");
        assertNotEquals(p1, p3, "Полиномы с коэффициентами за пределами эпсилон не должны быть равны");
    }

    @Test
    @DisplayName("Тест метода hashCode")
    void testHashCode() {
        Polynomial p1 = new Polynomial(1.0, 2.0, 3.0);
        Polynomial p2 = new Polynomial(1.0, 2.0, 3.0);
        
        assertEquals(p1.hashCode(), p2.hashCode(), "Равные полиномы должны иметь одинаковые хэш-коды");
        
        // Проверяем согласованность
        int hash1 = p1.hashCode();
        int hash2 = p1.hashCode();
        assertEquals(hash1, hash2, "Хэш-код должен быть согласованным");
    }

    @Test
    @DisplayName("Test add method")
    void testAdd() {
        Polynomial p1 = new Polynomial(1.0, 2.0, 3.0); // 3x^2 + 2x + 1
        Polynomial p2 = new Polynomial(4.0, 5.0, 6.0); // 6x^2 + 5x + 4
        Polynomial expected = new Polynomial(5.0, 7.0, 9.0); // 9x^2 + 7x + 5
        
        Polynomial result = p1.add(p2);
        assertEquals(expected, result, "Addition should be correct");
    }
    
    @Test
    @DisplayName("Test add method with different degrees")
    void testAddWithDifferentDegrees() {
        Polynomial p1 = new Polynomial(1.0, 2.0, 3.0); // 3x^2 + 2x + 1
        Polynomial p2 = new Polynomial(4.0, 5.0); // 5x + 4
        Polynomial expected = new Polynomial(5.0, 7.0, 3.0); // 3x^2 + 7x + 5
        
        Polynomial result = p1.add(p2);
        assertEquals(expected, result, "Addition with different degrees should be correct");
    }

    @Test
    @DisplayName("Test subtract method")
    void testSubtract() {
        Polynomial p1 = new Polynomial(5.0, 7.0, 9.0); // 9x^2 + 7x + 5
        Polynomial p2 = new Polynomial(1.0, 2.0, 3.0); // 3x^2 + 2x + 1
        Polynomial expected = new Polynomial(4.0, 5.0, 6.0); // 6x^2 + 5x + 4
        
        Polynomial result = p1.subtract(p2);
        assertEquals(expected, result, "Subtraction should be correct");
    }
    
    @Test
    @DisplayName("Test subtract method with different degrees")
    void testSubtractWithDifferentDegrees() {
        Polynomial p1 = new Polynomial(5.0, 7.0, 9.0); // 9x^2 + 7x + 5
        Polynomial p2 = new Polynomial(1.0, 2.0); // 2x + 1
        Polynomial expected = new Polynomial(4.0, 5.0, 9.0); // 9x^2 + 5x + 4
        
        Polynomial result = p1.subtract(p2);
        assertEquals(expected, result, "Subtraction with different degrees should be correct");
    }

    @Test
    @DisplayName("Test multiply polynomial method")
    void testMultiplyPolynomial() {
        Polynomial p1 = new Polynomial(1.0, 2.0); // 2x + 1
        Polynomial p2 = new Polynomial(3.0, 4.0); // 4x + 3
        Polynomial expected = new Polynomial(3.0, 10.0, 8.0); // 8x^2 + 10x + 3
        
        Polynomial result = p1.multiply(p2);
        assertEquals(expected, result, "Multiplication should be correct");
    }
    
    @Test
    @DisplayName("Test multiply polynomial with zero coefficients")
    void testMultiplyPolynomialWithZeros() {
        Polynomial p1 = new Polynomial(1.0, 0.0, 3.0); // 3x^2 + 1
        Polynomial p2 = new Polynomial(0.0, 2.0); // 2x
        Polynomial expected = new Polynomial(0.0, 2.0, 0.0, 6.0); // 6x^3 + 2x
        
        Polynomial result = p1.multiply(p2);
        assertEquals(expected, result, "Multiplication with zero coefficients should be correct");
    }

    @Test
    @DisplayName("Test multiply scalar method")
    void testMultiplyScalar() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0); // 3x^2 + 2x + 1
        Polynomial expected = new Polynomial(2.0, 4.0, 6.0); // 6x^2 + 4x + 2
        
        Polynomial result = p.multiply(2.0);
        assertEquals(expected, result, "Scalar multiplication should be correct");
    }
    
    @Test
    @DisplayName("Test multiply by zero scalar")
    void testMultiplyZeroScalar() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0); // 3x^2 + 2x + 1
        Polynomial expected = new Polynomial(0.0); // 0
        
        Polynomial result = p.multiply(0.0);
        assertEquals(expected, result, "Multiplication by zero should give zero polynomial");
    }
    
    @Test
    @DisplayName("Test multiply by almost zero scalar")
    void testMultiplyAlmostZeroScalar() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0); // 3x^2 + 2x + 1
        Polynomial expected = new Polynomial(0.0); // 0
        
        Polynomial result = p.multiply(1e-11);
        assertEquals(expected, result, "Multiplication by almost zero should give zero polynomial");
    }

    @Test
    @DisplayName("Test divide scalar method")
    void testDivideScalar() {
        Polynomial p = new Polynomial(2.0, 4.0, 6.0); // 6x^2 + 4x + 2
        Polynomial expected = new Polynomial(1.0, 2.0, 3.0); // 3x^2 + 2x + 1
        
        Polynomial result = p.divide(2.0);
        assertEquals(expected, result, "Scalar division should be correct");
    }
    
    @Test
    @DisplayName("Test divide by zero scalar")
    void testDivideZeroScalar() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            p.divide(0.0);
        }, "Division by zero should throw IllegalArgumentException");
        
        assertTrue(exception.getMessage().contains("Division by zero"), 
                   "Exception message should mention division by zero");
    }
    
    @Test
    @DisplayName("Test divide by almost zero scalar")
    void testDivideAlmostZeroScalar() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            p.divide(1e-11);
        }, "Division by almost zero should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Test evaluate method")
    void testEvaluate() {
        Polynomial p = new Polynomial(1.0, 2.0, 3.0); // 3x^2 + 2x + 1
        
        assertEquals(1.0, p.evaluate(0.0), 1e-10, "p(0) should be 1");
        assertEquals(6.0, p.evaluate(1.0), 1e-10, "p(1) should be 6");
        assertEquals(17.0, p.evaluate(2.0), 1e-10, "p(2) should be 17");
    }
    
    @Test
    @DisplayName("Test evaluate with optimization for x=0")
    void testEvaluateWithXZero() {
        Polynomial p = new Polynomial(5.0, 10.0, 15.0); // 15x^2 + 10x + 5
        
        assertEquals(5.0, p.evaluate(0.0), 1e-10, "p(0) should be 5");
        assertEquals(5.0, p.evaluate(1e-11), 1e-10, "p(≈0) should be approximately 5");
    }
    
    @Test
    @DisplayName("Test evaluate with constant polynomial")
    void testEvaluateConstantPolynomial() {
        Polynomial p = new Polynomial(7.0); // 7
        
        assertEquals(7.0, p.evaluate(0.0), 1e-10, "p(0) should be 7");
        assertEquals(7.0, p.evaluate(10.0), 1e-10, "p(10) should be 7");
    }

    // Helper method to provide arguments for parameterized tests
    static Stream<Arguments> providePolynomialsForAddition() {
        return Stream.of(
            Arguments.of(
                new Polynomial(1.0, 2.0, 3.0),
                new Polynomial(4.0, 5.0, 6.0),
                new Polynomial(5.0, 7.0, 9.0)
            ),
            Arguments.of(
                new Polynomial(1.0, 2.0),
                new Polynomial(3.0, 4.0, 5.0),
                new Polynomial(4.0, 6.0, 5.0)
            ),
            Arguments.of(
                new Polynomial(0.0),
                new Polynomial(1.0, 2.0, 3.0),
                new Polynomial(1.0, 2.0, 3.0)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("providePolynomialsForAddition")
    @DisplayName("Test addition with parameterized inputs")
    void testAddParameterized(Polynomial p1, Polynomial p2, Polynomial expected) {
        Polynomial result = p1.add(p2);
        assertEquals(expected, result, "Addition result should match expected polynomial");
    }

    @Test
    @DisplayName("Test lastCoefficient method through degree and evaluation")
    void testLastCoefficientMethod() {
        // Create an empty polynomial
        Polynomial emptyPoly = new Polynomial(new ArrayList<>());
        assertEquals(0, emptyPoly.degree(), "Empty polynomial should have degree 0");
        assertEquals(0.0, emptyPoly.evaluate(1.0), "Empty polynomial should evaluate to 0");
        
        // Create a polynomial and then remove all coefficients to make it empty
        Polynomial p = new Polynomial(1.0, 2.0);
        try {
            java.lang.reflect.Method method = Polynomial.class.getDeclaredMethod(
                "updateCoefficients", List.class);
            method.setAccessible(true);
            method.invoke(p, new ArrayList<>());
            
            assertEquals(0, p.degree(), "Polynomial with empty coefficients should have degree 0");
            assertEquals(0.0, p.evaluate(1.0), "Polynomial with empty coefficients should evaluate to 0");
        } catch (Exception e) {
            fail("Could not access updateCoefficients method: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test directly accessing lastCoefficient method")
    void testLastCoefficientDirectAccess() {
        // Create polynomials of different degrees
        Polynomial p0 = new Polynomial(5.0); // degree 0
        Polynomial p1 = new Polynomial(1.0, 3.0); // degree 1
        Polynomial p2 = new Polynomial(2.0, 4.0, 6.0); // degree 2
        Polynomial pEmpty = new Polynomial(new ArrayList<>());
        
        try {
            // Use reflection to access the private lastCoefficient method
            java.lang.reflect.Method method = Polynomial.class.getDeclaredMethod("lastCoefficient");
            method.setAccessible(true);
            
            // Test for polynomial of degree 0
            double coeff0 = (double) method.invoke(p0);
            assertEquals(5.0, coeff0, "lastCoefficient should return the coefficient for degree 0");
            
            // Test for polynomial of degree 1
            double coeff1 = (double) method.invoke(p1);
            assertEquals(3.0, coeff1, "lastCoefficient should return the coefficient for degree 1");
            
            // Test for polynomial of degree 2
            double coeff2 = (double) method.invoke(p2);
            assertEquals(6.0, coeff2, "lastCoefficient should return the coefficient for degree 2");
            
            // Test for empty polynomial
            double coeffEmpty = (double) method.invoke(pEmpty);
            assertEquals(0.0, coeffEmpty, "lastCoefficient should return 0.0 for empty polynomial");
            
        } catch (Exception e) {
            fail("Could not access lastCoefficient method: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test handling of null and empty coefficients in constructor")
    void testHandlingEmptyCoefficientsInConstructor() {
        // Test with null coefficients list
        assertThrows(NullPointerException.class, () -> new Polynomial((List<Double>) null), 
                     "Constructor should throw NullPointerException for null list");
        
        // Test with empty list
        Polynomial pEmpty = new Polynomial(new ArrayList<>());
        assertEquals(0, pEmpty.degree(), "Empty polynomial should have degree 0");
        assertEquals(List.of(0.0), pEmpty.getCoefficients(), "Empty polynomial should have [0.0] coefficients");
    }

    @Test
    @DisplayName("Test lastCoefficient method with negative degree")
    void testLastCoefficientWithNegativeDegree() {
        Polynomial p = new Polynomial(1.0, 2.0);
        
        try {
            // First, create a field accessor to manipulate the coefficients directly
            java.lang.reflect.Field coefficientsField = Polynomial.class.getDeclaredField("coefficients");
            coefficientsField.setAccessible(true);
            
            // Replace the coefficients with an empty list to simulate negative degree situation
            List<Double> emptyList = new ArrayList<>();
            coefficientsField.set(p, emptyList);
            
            // Now call the degree method - it should return -1
            assertEquals(-1, p.degree(), "Degree should be -1 when coefficients list is empty");
            
            // Now test the lastCoefficient method
            java.lang.reflect.Method lastCoeffMethod = Polynomial.class.getDeclaredMethod("lastCoefficient");
            lastCoeffMethod.setAccessible(true);
            double result = (double) lastCoeffMethod.invoke(p);
            
            // The lastCoefficient method should return 0.0 when degree is negative
            assertEquals(0.0, result, "lastCoefficient should return 0.0 when degree is negative");
            
        } catch (Exception e) {
            fail("Exception while testing lastCoefficient with negative degree: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test adding 0.0 to empty coefficients list")
    void testAddingZeroToEmptyCoefficients() {
        try {
            // Create a polynomial with an initially empty list
            Polynomial p = new Polynomial();
            
            // Get access to the coefficients field
            java.lang.reflect.Field coefficientsField = Polynomial.class.getDeclaredField("coefficients");
            coefficientsField.setAccessible(true);
            
            // First verify the initial state has [0.0]
            List<Double> initialCoeffs = (List<Double>) coefficientsField.get(p);
            assertEquals(List.of(0.0), initialCoeffs, "Initial coefficients should be [0.0]");
            
            // Now set the coefficients to an empty list
            coefficientsField.set(p, new ArrayList<>());
            
            // Verify that the list is now empty
            List<Double> emptyList = (List<Double>) coefficientsField.get(p);
            assertTrue(emptyList.isEmpty(), "Coefficients list should be empty");
            
            // Now trigger the code path by calling a method that uses updateCoefficients
            java.lang.reflect.Method updateMethod = Polynomial.class.getDeclaredMethod("updateCoefficients", List.class);
            updateMethod.setAccessible(true);
            updateMethod.invoke(p, new ArrayList<>());
            
            // Verify that 0.0 was added to the empty list
            List<Double> resultList = (List<Double>) coefficientsField.get(p);
            assertEquals(1, resultList.size(), "Coefficients list should have one element");
            assertEquals(0.0, resultList.get(0), "The element should be 0.0");
            
        } catch (Exception e) {
            fail("Exception while testing adding 0.0 to empty coefficients: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test creating a polynomial with empty coefficients list that gets filled with zero")
    void testEmptyCoefficientsAddingZero() {
        List<Double> emptyList = new ArrayList<>();
        Polynomial p = new Polynomial(emptyList);
        
        assertEquals(0, p.degree(), "Polynomial with empty coefficients should have degree 0");
        assertEquals(List.of(0.0), p.getCoefficients(), "Polynomial with empty coefficients should have [0.0] after construction");
    }

    @Test
    @DisplayName("Test varargs constructor with all zero coefficients resulting in empty list that gets filled with zero")
    void testVarargsConstructorWithAllZerosAddingZero() {
        // Create a polynomial with all zeros, which after trimming will be empty
        Polynomial p = new Polynomial(0.0, 0.0, 0.0);
        
        assertEquals(0, p.degree(), "Polynomial with all zero coefficients should have degree 0");
        assertEquals(List.of(0.0), p.getCoefficients(), "Polynomial with all zero coefficients should have [0.0] after trimming");
    }

    @Test
    @DisplayName("Test varargs constructor with single zero coefficient")
    void testVarargsConstructorWithSingleZero() {
        Polynomial p = new Polynomial(0.0);
        assertEquals(0, p.degree(), "Polynomial with single zero coefficient should have degree 0");
        assertEquals(List.of(0.0), p.getCoefficients(), "Coefficients should be [0.0]");
    }

    @Test
    @DisplayName("Тест проверки на дубликаты x-координат")
    void testCheckForDuplicateXAfterSort() throws Exception {
        // Получаем метод через рефлексию
        java.lang.reflect.Method method = Polynomial.class.getDeclaredMethod(
            "checkForDuplicateXAfterSort", List.class);
        method.setAccessible(true);

        // Тест с корректными точками
        List<Point> points = Arrays.asList(
            new Point(0.0, 1.0),
            new Point(1.0, 2.0),
            new Point(2.0, 3.0)
        );
        List<Point> finalPoints2 = points;
        assertDoesNotThrow(() -> {
            try {
                method.invoke(null, finalPoints2);
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (e.getCause() instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e.getCause();
                }
                throw e;
            }
        }, "Should not throw exception for unique x values");

        // Тест с дублирующимися x-координатами
        points = Arrays.asList(
            new Point(0.0, 1.0),
            new Point(0.0, 2.0),
            new Point(1.0, 3.0)
        );
        List<Point> finalPoints1 = points;
        Exception exception = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> 
            method.invoke(null, finalPoints1), 
            "Should throw InvocationTargetException wrapping IllegalArgumentException for duplicate x values");
        
        assertTrue(exception.getCause() instanceof IllegalArgumentException, 
            "The wrapped exception should be IllegalArgumentException");
        assertTrue(exception.getCause().getMessage().contains("Duplicate x values"),
            "Exception message should mention duplicate x values");

        // Тест с x-координатами, отличающимися меньше чем на epsilon
        points = Arrays.asList(
            new Point(0.0, 1.0),
            new Point(0.0 + EPSILON/2, 2.0),
            new Point(1.0, 3.0)
        );
        List<Point> finalPoints = points;
        exception = assertThrows(java.lang.reflect.InvocationTargetException.class, () ->
            method.invoke(null, finalPoints),
            "Should throw InvocationTargetException wrapping IllegalArgumentException for x values within epsilon");
        
        assertTrue(exception.getCause() instanceof IllegalArgumentException, 
            "The wrapped exception should be IllegalArgumentException");
        assertTrue(exception.getCause().getMessage().contains("Duplicate x values"),
            "Exception message should mention duplicate x values");
    }

    @Test
    @DisplayName("Тест сортировки точек по x-координате")
    void testSortPointsByX() throws Exception {
        // Получаем метод через рефлексию
        java.lang.reflect.Method method = Polynomial.class.getDeclaredMethod(
            "sortPointsByX", List.class);
        method.setAccessible(true);

        // Тест с неотсортированными точками
        List<Point> points = Arrays.asList(
            new Point(2.0, 1.0),
            new Point(0.0, 2.0),
            new Point(1.0, 3.0)
        );
        @SuppressWarnings("unchecked")
        List<Point> sorted = (List<Point>) method.invoke(null, points);
        assertEquals(0.0, sorted.get(0).getX(), "First point should have smallest x");
        assertEquals(1.0, sorted.get(1).getX(), "Second point should have middle x");
        assertEquals(2.0, sorted.get(2).getX(), "Last point should have largest x");

        // Тест с уже отсортированными точками
        points = Arrays.asList(
            new Point(0.0, 1.0),
            new Point(1.0, 2.0),
            new Point(2.0, 3.0)
        );
        @SuppressWarnings("unchecked")
        List<Point> sorted2 = (List<Point>) method.invoke(null, points);
        assertEquals(points, sorted2, "Already sorted points should remain unchanged");

        // Тест с точками в обратном порядке
        points = Arrays.asList(
            new Point(2.0, 1.0),
            new Point(1.0, 2.0),
            new Point(0.0, 3.0)
        );
        @SuppressWarnings("unchecked")
        List<Point> sorted3 = (List<Point>) method.invoke(null, points);
        assertEquals(0.0, sorted3.get(0).getX(), "First point should have smallest x");
        assertEquals(1.0, sorted3.get(1).getX(), "Second point should have middle x");
        assertEquals(2.0, sorted3.get(2).getX(), "Last point should have largest x");
    }

    @Test
    @DisplayName("Test varargs constructor with zero-length array (empty)")
    void testVarargsConstructorWithEmptyArray() {
        // This would create an empty coefficients array, which should add 0.0
        double[] emptyArray = new double[0];
        Polynomial p = new Polynomial(emptyArray);
        
        assertEquals(0, p.degree(), "Polynomial with zero-length array should have degree 0");
        assertEquals(List.of(0.0), p.getCoefficients(), "Polynomial with zero-length array should have [0.0]");
    }
} 