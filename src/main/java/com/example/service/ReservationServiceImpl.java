package com.example.service;

import com.example.model.Reservation;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import java.util.List;

/**
 * Implémentation du service ReservationService avec gestion transactionnelle
 * et détection du verrouillage optimiste.
 */
public class ReservationServiceImpl implements ReservationService {

    private final EntityManagerFactory emf;

    public ReservationServiceImpl() {
        this.emf = Persistence.createEntityManagerFactory("reservationPU");
    }

    public ReservationServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Reservation creerReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation modifierReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // Utilisation de em.merge pour vérifier la concordance de @Version
            Reservation managed = em.merge(reservation);
            em.getTransaction().commit();
            return managed;
        } catch (OptimisticLockException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof OptimisticLockException) {
                    throw (OptimisticLockException) cause;
                }
                cause = cause.getCause();
            }
            throw new RuntimeException("Erreur lors de la modification de la réservation : " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation trouverParId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Reservation.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> listerReservations() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
