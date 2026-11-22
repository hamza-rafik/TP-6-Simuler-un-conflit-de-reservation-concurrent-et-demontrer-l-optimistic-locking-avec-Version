package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import javax.persistence.OptimisticLockException;

/**
 * Gestionnaire de retry automatique en cas d'OptimisticLockException.
 */
public class ReservationRetryHandler {

    private static final int MAX_RETRIES = 3;
    private final ReservationService service;

    public ReservationRetryHandler(ReservationService service) {
        this.service = service;
    }

    public Reservation modifierAvecRetry(Reservation reservation) {
        int tentative = 0;
        String cibleSalle = reservation.getSalle();
        while (tentative < MAX_RETRIES) {
            try {
                return service.modifierReservation(reservation);
            } catch (OptimisticLockException e) {
                tentative++;
                System.out.println("Conflit détecté, tentative de retry " + tentative + "/" + MAX_RETRIES + "...");
                // Recharger la réservation pour obtenir la dernière version persistée
                Reservation derniereVersion = service.trouverParId(reservation.getId());
                if (derniereVersion == null) {
                    throw new IllegalStateException("La réservation n'existe plus en base.");
                }
                reservation = derniereVersion;
                // Réappliquer la modification souhaitée sur l'entité à jour
                reservation.setSalle(cibleSalle);
            }
        }
        throw new OptimisticLockException("Échec après " + MAX_RETRIES + " tentatives de résolution.");
    }
}
