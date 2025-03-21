package org.itmo.testing.lab2.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatusServiceTest {

    private UserAnalyticsService userAnalyticsService;
    private UserStatusService userStatusService;

    @BeforeAll
    void setUp() {
        userAnalyticsService = mock(UserAnalyticsService.class);
        userStatusService = new UserStatusService(userAnalyticsService);
    }

    @Test
    public void testGetUserStatus_Active() {
        // Настроим поведение mock-объекта
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(60L);

        String status = userStatusService.getUserStatus("user123");

        assertEquals("Active", status);
    }

    @Test
    public void testGetUserStatus_HighlyActive() {
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(120L);

        String status = userStatusService.getUserStatus("user123");

        assertEquals("Highly active", status);
    }

    @Test
    public void testGetUserStatus_InActive() {
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(59L);

        String status = userStatusService.getUserStatus("user123");

        assertEquals("Inactive", status);
    }

    @Test
    public void testGetUserLastSessionDate_withNoSessions_shouldThrowException() {
        when(userAnalyticsService.getUserSessions("user123")).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> userStatusService.getUserLastSessionDate("user123"));
    }

    @Test
    public void testGetUserLastSessionDate_withOneSession_shouldReturnSessionLogoutTimeDate() {
        UserAnalyticsService.Session session = new UserAnalyticsService.Session(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
        );

        when(userAnalyticsService.getUserSessions("user123")).thenReturn(List.of(session));
        var date = userStatusService.getUserLastSessionDate("user123");

        assertTrue(date.isPresent());
        assertEquals(session.getLogoutTime().toLocalDate().toString(), date.get());
    }

}
