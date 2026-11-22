package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;

/**
 * Simulation d'un conflit de réservation concurrent démontrant le verrouillage optimiste.
 */
public class ConcurrentReservationSimulator {

    public static void main(String[] args) {
        runSimulation(new ReservationServiceImpl());
    }

    public static void runSimulation(ReservationService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  SCÉNARIO 1 : SIMULATION D'UN CONFLIT CONCURRENT (OptimisticLockException)");
        System.out.println("--------------------------------------------------------------------------------");

        // 1. Création initiale d'une réservation
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = debut.plusHours(2);
        Reservation reservation = new Reservation("Salle A", debut, fin);
        reservation = service.creerReservation(reservation);
        System.out.println("[État Initial] Réservation créée : " + reservation);

        // 2. Deux transactions concurrentes chargent la même réservation (même version = 0)
        Reservation tx1 = service.trouverParId(reservation.getId());
        Reservation tx2 = service.trouverParId(reservation.getId());

        System.out.println("Tx1 a chargé : Salle=" + tx1.getSalle() + ", Version=" + tx1.getVersion());
        System.out.println("Tx2 a chargé : Salle=" + tx2.getSalle() + ", Version=" + tx2.getVersion());

        // 3. Transaction 1 modifie la réservation et commit en premier
        tx1.setSalle("Salle B");
        service.modifierReservation(tx1);
        System.out.println("Transaction 1 : modification réussie (Salle B, nouvelle version en base = " + (tx1.getVersion() + 1) + ").");

        // 4. Transaction 2 tente de modifier avec sa version devenue obsolète
        try {
            tx2.setSalle("Salle C");
            service.modifierReservation(tx2);
            System.out.println("Transaction 2 : modification réussie.");
        } catch (OptimisticLockException e) {
            System.err.println("Transaction 2 : conflit détecté - " + e.getMessage());
            System.out.println(">> Confirmation : L'annotation @Version a correctement rejeté la mise à jour concurrente obsolète !");
        }
    }
}
