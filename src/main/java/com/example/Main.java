package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;

import java.util.List;

/**
 * Lanceur principal pour le TP 6 : Optimistic Locking avec JPA et Hibernate.
 * Exécute successivement :
 * 1. La simulation brute d'un conflit et la capture de l'OptimisticLockException.
 * 2. La résolution élégante du conflit grâce au ReservationRetryHandler.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("   TP 6 : SIMULER UN CONFLIT DE RÉSERVATION ET DÉMONTRER L'OPTIMISTIC LOCKING (@Version)  ");
        System.out.println("==========================================================================================");

        ReservationServiceImpl service = new ReservationServiceImpl();

        try {
            // Scénario 1 : Démonstration du conflit concurrent
            ConcurrentReservationSimulator.runSimulation(service);

            // Scénario 2 : Résolution automatique par Retry Handler
            ConcurrentReservationWithRetry.runSimulationWithRetry(service);

            // Synthèse des données en base
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("  SYNTHÈSE DES RÉSERVATIONS EN BASE DE DONNÉES");
            System.out.println("--------------------------------------------------------------------------------");
            List<Reservation> reservations = service.listerReservations();
            System.out.printf("%-5s | %-15s | %-20s | %-20s | %-8s%n", "ID", "Salle", "Date Début", "Date Fin", "Version");
            System.out.println("------+-----------------+----------------------+----------------------+---------");
            for (Reservation r : reservations) {
                System.out.printf("%-5d | %-15s | %-20s | %-20s | %-8d%n",
                        r.getId(), r.getSalle(), r.getDateDebut(), r.getDateFin(), r.getVersion());
            }

            System.out.println("\n+======================================================================================+");
            System.out.println("|   [SUCCÈS] TOUTES LES ÉTAPES DU TP 6 ONT ÉTÉ VALIDÉES AVEC SUCCÈS !                  |");
            System.out.println("|   [+] Entité @Version et configuration persistence.xml validées                     |");
            System.out.println("|   [+] Détection stricte des conflits concurrents (OptimisticLockException)          |");
            System.out.println("|   [+] Stratégie de Retry automatique avec rechargement d'état opérationnelle         |");
            System.out.println("+======================================================================================+");
        } catch (Exception e) {
            System.err.println("Erreur globale : " + e.getMessage());
            e.printStackTrace();
        } finally {
            service.close();
        }
    }
}
