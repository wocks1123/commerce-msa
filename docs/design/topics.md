# Kafka 토픽

| Topic                      | Publisher         | Subscriber        |
|----------------------------|-------------------|-------------------|
| `order.aborted`            | order-service     | inventory-service |
| `order.expired`            | order-service     | inventory-service |
| `order.paid`               | order-service     | inventory-service |
| `product.registered`       | product-service   | inventory-service |
| `product.activated`        | product-service   | -                 |
| `product.deactivated`      | product-service   | -                 |
| `product.discontinued`     | product-service   | -                 |
| `product.modified`         | product-service   | -                 |
| `stock.reservation.failed` | inventory-service | order-service     |
| `payment.initialized`      | payment-service   | order-service     |
| `payment.approved`         | payment-service   | order-service     |
| `payment.failed`           | payment-service   | order-service     |
| `payment.expired`          | payment-service   | order-service     |
