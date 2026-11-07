package org.delcom.ifs25009pbo2026springbootcashflow.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Memberi cache panjang (1 tahun) untuk aset statis berversi hash (bundle Vaadin, font)
 * agar kunjungan ulang cepat dan audit "efficient cache policy" lolos.
 */
@Component
public class StaticCacheFilter extends OncePerRequestFilter {
    private static final String CACHE_VALUE = "public, max-age=31536000, immutable";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/VAADIN/build/") || path.contains("/fonts/")) {
            response.setHeader("Cache-Control", CACHE_VALUE);
            chain.doFilter(request, new HttpServletResponseWrapper(response) {
                @Override
                public void setHeader(String name, String value) {
                    if (!isCacheHeader(name)) {
                        super.setHeader(name, value);
                    }
                }

                @Override
                public void addHeader(String name, String value) {
                    if (!isCacheHeader(name)) {
                        super.addHeader(name, value);
                    }
                }

                private boolean isCacheHeader(String name) {
                    return "Cache-Control".equalsIgnoreCase(name)
                            || "Expires".equalsIgnoreCase(name)
                            || "Pragma".equalsIgnoreCase(name);
                }
            });
            return;
        }
        chain.doFilter(request, response);
    }
}
