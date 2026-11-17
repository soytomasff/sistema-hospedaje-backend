package com.amauta.amauta_backend.service;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class CheckoutScheduler {
    private final CheckoutService service;
    public CheckoutScheduler(CheckoutService service) { this.service = service; }
    @Scheduled(fixedDelayString = "${app.checkout.revision-ms:30000}")
    @EventListener(ApplicationReadyEvent.class)
    public void revisar() { service.revisarPendientes(); }
}
