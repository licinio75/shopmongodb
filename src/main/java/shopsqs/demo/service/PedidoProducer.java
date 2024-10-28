package shopsqs.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PedidoProducer {

    private static final String TOPIC = "orders";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void enviarMensaje(String mensaje) {
        kafkaTemplate.send(TOPIC, mensaje);
    }
}
