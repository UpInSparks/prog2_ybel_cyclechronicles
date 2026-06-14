package cyclechronicles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShopRepairDeliverTest {

    private Shop shop;

    @BeforeEach
    void setUp() {
        shop = new Shop();
    }

    private Order mockOrder(Type type, String customer) {
        Order o = mock(Order.class);
        when(o.getBicycleType()).thenReturn(type);
        when(o.getCustomer()).thenReturn(customer);
        return o;
    }

    // -----------------------------------------------------------------------
    // Shop#repair – leere Queue
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("repair: Leere Queue → Optional.empty()")
    void repair_emptyQueue_returnsEmpty() {
        Optional<Order> result = shop.repair();
        assertTrue(result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Shop#repair – ein Auftrag vorhanden
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("repair: Ein Auftrag vorhanden → wird zurückgegeben")
    void repair_oneOrder_returnsOrder() {
        Order o = mockOrder(Type.RACE, "Alice");
        shop.accept(o);

        Optional<Order> result = shop.repair();

        assertTrue(result.isPresent());
        assertSame(o, result.get());
    }

    // -----------------------------------------------------------------------
    // Shop#repair – FIFO-Reihenfolge
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("repair: FIFO – ältester Auftrag wird zuerst repariert")
    void repair_fifoOrder() {
        Order first = mockOrder(Type.RACE, "Alice");
        Order second = mockOrder(Type.FIXIE, "Bob");
        shop.accept(first);
        shop.accept(second);

        Optional<Order> result = shop.repair();

        assertTrue(result.isPresent());
        assertSame(first, result.get());
    }

    // -----------------------------------------------------------------------
    // Shop#repair – Auftrag wandert in completedOrders
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("repair: Reparierter Auftrag kann danach ausgeliefert werden")
    void repair_orderMovesToCompleted() {
        Order o = mockOrder(Type.RACE, "Alice");
        shop.accept(o);

        shop.repair();

        // Auftrag muss jetzt in completedOrders sein → deliver muss ihn finden
        Optional<Order> delivered = shop.deliver("Alice");
        assertTrue(delivered.isPresent());
        assertSame(o, delivered.get());
    }

    // -----------------------------------------------------------------------
    // Shop#repair – nach Reparatur ist pendingOrders kleiner
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("repair: Nach Reparatur kann neuer Auftrag desselben Kunden angenommen werden")
    void repair_freesSlotForSameCustomer() {
        Order first = mockOrder(Type.RACE, "Alice");
        shop.accept(first);

        shop.repair(); // Alice's Auftrag ist jetzt fertig, nicht mehr pending

        // Neuer Auftrag von Alice: kein Doppel mehr in pendingOrders
        Order second = mockOrder(Type.FIXIE, "Alice");
        assertTrue(shop.accept(second));
    }

    // -----------------------------------------------------------------------
    // Shop#deliver – kein passender Auftrag
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("deliver: Kein passender Auftrag → Optional.empty()")
    void deliver_noMatchingOrder_returnsEmpty() {
        Optional<Order> result = shop.deliver("Unknown");
        assertTrue(result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Shop#deliver – passender Auftrag vorhanden
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("deliver: Fertiggestellter Auftrag wird ausgeliefert")
    void deliver_completedOrder_returned() {
        Order o = mockOrder(Type.RACE, "Alice");
        shop.accept(o);
        shop.repair();

        Optional<Order> result = shop.deliver("Alice");

        assertTrue(result.isPresent());
        assertSame(o, result.get());
    }

    // -----------------------------------------------------------------------
    // Shop#deliver – Auftrag wird aus completedOrders entfernt
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("deliver: Ausgelieferter Auftrag wird aus completedOrders entfernt")
    void deliver_removesOrderFromCompleted() {
        Order o = mockOrder(Type.RACE, "Alice");
        shop.accept(o);
        shop.repair();

        shop.deliver("Alice"); // erste Auslieferung

        // Zweite Auslieferung für denselben Kunden: kein Auftrag mehr vorhanden
        Optional<Order> second = shop.deliver("Alice");
        assertTrue(second.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Shop#deliver – falscher Kunde → leer
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("deliver: Falscher Kundenname → Optional.empty()")
    void deliver_wrongCustomer_returnsEmpty() {
        Order o = mockOrder(Type.RACE, "Alice");
        shop.accept(o);
        shop.repair();

        Optional<Order> result = shop.deliver("Bob");
        assertTrue(result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Zusammenspiel: kompletter Workflow
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Workflow: accept → repair → deliver für zwei Kunden")
    void fullWorkflow_twoCustomers() {
        Order o1 = mockOrder(Type.RACE, "Alice");
        Order o2 = mockOrder(Type.FIXIE, "Bob");

        shop.accept(o1);
        shop.accept(o2);

        Optional<Order> r1 = shop.repair(); // repariert Alice (FIFO)
        assertTrue(r1.isPresent());
        assertSame(o1, r1.get());

        Optional<Order> r2 = shop.repair(); // repariert Bob
        assertTrue(r2.isPresent());
        assertSame(o2, r2.get());

        assertSame(o1, shop.deliver("Alice").get());
        assertSame(o2, shop.deliver("Bob").get());
    }
}
