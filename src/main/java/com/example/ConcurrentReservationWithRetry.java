package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;

import java.time.LocalDateTime;

/**
 * Test de la stratégie de retry lors d'un conflit concurrent.
 */
public class ConcurrentReservationWithRetry {

    public static void main(String[] args) {
        runSimulationWithRetry(new ReservationServiceImpl());
    }

    public static void runSimulationWithRetry(ReservationService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  SCÉNARIO 2 : RÉSOLUTION DU CONFLIT AVEC RETRY HANDLER (Auto-Recupération)");
        System.out.println("--------------------------------------------------------------------------------");

        ReservationRetryHandler retryHandler = new ReservationRetryHandler(service);

        // 1. Création d'une nouvelle réservation
        LocalDateTime debut = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = debut.plusHours(3);
        Reservation reservation = new Reservation("Salle A", debut, fin);
        reservation = service.creerReservation(reservation);
        System.out.println("[État Initial] Réservation créée : " + reservation);

        // 2. Chargement concurrent par deux transactions (version = 0)
        Reservation tx1 = service.trouverParId(reservation.getId());
        Reservation tx2 = service.trouverParId(reservation.getId());

        // 3. Transaction 1 modifie en "Salle B" et commit
        tx1.setSalle("Salle B");
        service.modifierReservation(tx1);
        System.out.println("Transaction 1 : modification réussie vers 'Salle B'.");

        // 4. Transaction 2 tente de modifier vers "Salle C" avec le RetryHandler
        tx2.setSalle("Salle C");
        try {
            Reservation resultat = retryHandler.modifierAvecRetry(tx2);
            System.out.println("Transaction 2 : modification réussie après retry ! Salle finale=" + resultat.getSalle() + ", Version finale=" + resultat.getVersion());
        } catch (Exception e) {
            System.err.println("Transaction 2 : échec après retries - " + e.getMessage());
        }
    }
}
