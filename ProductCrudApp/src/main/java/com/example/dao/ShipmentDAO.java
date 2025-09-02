package com.example.dao;

import com.example.entities.Order;
import com.example.entities.Shipment;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

@Stateless
public class ShipmentDAO extends BaseDAO<Shipment> {

    public ShipmentDAO() {
        super(Shipment.class);
    }

    /**
     * Find a shipment by its order.
     * Returns null if no shipment exists.
     */
    public Shipment findByOrder(Order order) {
        try {
            return em.createQuery(
                    "SELECT s FROM Shipment s WHERE s.order = :order", Shipment.class)
                    .setParameter("order", order)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}