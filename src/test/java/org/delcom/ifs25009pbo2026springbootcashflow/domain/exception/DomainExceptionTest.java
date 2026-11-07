package org.delcom.ifs25009pbo2026springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainExceptionTest {

    @Test
    void domainExceptionKeepsMessageAndCause() {
        RuntimeException cause = new RuntimeException("akar");
        DomainException ex = new DomainException("pesan", cause);
        assertEquals("pesan", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals("polos", new DomainException("polos").getMessage());
    }

    @Test
    void entityNotFoundExceptionCarriesId() {
        EntityNotFoundException ex = new EntityNotFoundException(7);
        assertEquals(7, ex.getId());
        assertEquals("Transaksi dengan ID 7 tidak ditemukan.", ex.getMessage());
        assertTrue(ex instanceof DomainException);
    }

    @Test
    void validationExceptionIsDomainException() {
        ValidationException ex = new ValidationException("salah");
        assertEquals("salah", ex.getMessage());
        assertTrue(ex instanceof DomainException);
    }
}
