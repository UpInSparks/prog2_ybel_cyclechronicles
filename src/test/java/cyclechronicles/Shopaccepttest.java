package cyclechronicles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShopAcceptTest {

    private Shop shop;

    @BeforeEach
    void setUp() {
        shop = new Shop();
    }

    // -----------------------------------------------------------------------
    // Hilfsmethode: erstellt einen Mock-Auftrag
    // -----------------------------------------------------------------------

    private Order mockOrder(Type type, String customer) {
        Order o = mock(Order.class);
        when(o.getBicycleType()).thenReturn(type);
        when(o.getCustomer()).thenReturn(customer);
        return o;
    }

    // -----------------------------------------------------------------------
    // ÄK1: E-Bike → immer abgelehnt
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ÄK1: E-Bike wird abgelehnt")
    void ebike_isRejected() {
        Order o = mockOrder(Type.EBIKE, "Alice");
        assertFalse(shop.accept(o));
    }

    // -----------------------------------------------------------------------
    // ÄK2: Gravel-Bike → immer abgelehnt
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ÄK2: Gravel-Bike wird abgelehnt")
    void gravelBike_isRejected() {
        Order o = mockOrder(Type.GRAVEL, "Alice");
        assertFalse(shop.accept(o));
    }

    // -----------------------------------------------------------------------
    // ÄK3: gültige Fahrradtypen
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ÄK3a: Race-Bike wird angenommen (leere Queue)")
    void raceBike_isAccepted() {
        Order o = mockOrder(Type.RACE, "Alice");
        assertTrue(shop.accept(o));
    }

    @Test
    @DisplayName("ÄK3b: Single-Speed-Bike wird angenommen")
    void singleSpeedBike_isAccepted() {
        Order o = mockOrder(Type.SINGLE_SPEED, "Alice");
        assertTrue(shop.accept(o));
    }

    @Test
    @DisplayName("ÄK3c: Fixie wird angenommen")
    void fixie_isAccepted() {
        Order o = mockOrder(Type.FIXIE, "Alice");
        assertTrue(shop.accept(o));
    }

    // -----------------------------------------------------------------------
    // ÄK4: Doppelauftrag desselben Kunden → abgelehnt
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ÄK4: Zweiter Auftrag desselben Kunden wird abgelehnt")
    void duplicateCustomer_isRejected() {
        Order first = mockOrder(Type.RACE, "Bob");
        Order second = mockOrder(Type.FIXIE, "Bob");

        assertTrue(shop.accept(first));
        assertFalse(shop.accept(second));
    }

    // -----------------------------------------------------------------------
    // ÄK5: Verschiedene Kunden → jeweils angenommen
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ÄK5: Verschiedene Kunden werden beide angenommen")
    void differentCustomers_bothAccepted() {
        Order o1 = mockOrder(Type.RACE, "Alice");
        Order o2 = mockOrder(Type.FIXIE, "Carol");

        assertTrue(shop.accept(o1));
        assertTrue(shop.accept(o2));
    }

    // -----------------------------------------------------------------------
    // ÄK6/GW1: leere Warteschlange → Annahme möglich
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GW1: Leere Queue (0 Aufträge) – Annahme möglich")
    void emptyQueue_orderAccepted() {
        Order o = mockOrder(Type.RACE, "Alice");
        assertTrue(shop.accept(o));
    }

    // -----------------------------------------------------------------------
    // GW2: 3 vorhandene Aufträge → 4. wird angenommen
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GW2: 3 Aufträge in Queue – weiterer Auftrag wird angenommen")
    void threeOrders_fourthAccepted() {
        for (int i = 0; i < 3; i++) {
            shop.accept(mockOrder(Type.RACE, "Customer" + i));
        }
        Order o = mockOrder(Type.FIXIE, "NewCustomer");
        assertTrue(shop.accept(o));
    }

    // -----------------------------------------------------------------------
    // GW3: 4 vorhandene Aufträge → 5. wird noch angenommen (Grenzfall)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GW3: 4 Aufträge in Queue – 5. Auftrag wird noch angenommen (Grenze)")
    void fourOrders_fifthAccepted() {
        for (int i = 0; i < 4; i++) {
            shop.accept(mockOrder(Type.RACE, "Customer" + i));
        }
        Order o = mockOrder(Type.FIXIE, "FifthCustomer");
        assertTrue(shop.accept(o));
    }

    // -----------------------------------------------------------------------
    // GW4 / ÄK7: 5 vorhandene Aufträge → 6. wird abgelehnt (Queue voll)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GW4: 5 Aufträge in Queue – 6. Auftrag wird abgelehnt (Queue voll)")
    void fiveOrders_sixthRejected() {
        for (int i = 0; i < 5; i++) {
            shop.accept(mockOrder(Type.RACE, "Customer" + i));
        }
        Order o = mockOrder(Type.FIXIE, "SixthCustomer");
        assertFalse(shop.accept(o));
    }

    // -----------------------------------------------------------------------
    // Kombinationstest: volle Queue + falscher Typ
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Kombination: volle Queue + E-Bike → abgelehnt (Typ prüft zuerst)")
    void fullQueue_eBike_rejected() {
        for (int i = 0; i < 5; i++) {
            shop.accept(mockOrder(Type.RACE, "Customer" + i));
        }
        Order o = mockOrder(Type.EBIKE, "NewCustomer");
        assertFalse(shop.accept(o));
    }

    @Test
    @DisplayName("Kombination: Gravel-Bike + Doppelkunde → abgelehnt (Typ prüft zuerst)")
    void gravel_duplicateCustomer_rejected() {
        shop.accept(mockOrder(Type.RACE, "Alice"));
        Order duplicate = mockOrder(Type.GRAVEL, "Alice");
        assertFalse(shop.accept(duplicate));
    }
}
