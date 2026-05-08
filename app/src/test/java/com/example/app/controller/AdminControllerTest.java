package com.example.app.controller;

import com.example.dto.AdminDashboardStats;
import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import com.example.service.SongService;
import com.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ModelMap;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SongService songService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void home_validPageable_returnsAdminPageViewWithAllDashboardStats() {
        Pageable pageable = PageRequest.of(0, 5);
        ModelMap modelMap = new ModelMap();
        Object stats = setupDashboardStats(1000L, 10.5, 200L, 5.0, 50L, 3.2);
        Page<Object> topSongs = new PageImpl<>(List.of(new Object()));

        doReturn(stats).when(userService).getAdminDashboardStats();
        doReturn(topSongs).when(songService).getTopSongPopularityLastMonth(pageable);

        String view = adminController.home(modelMap, pageable);

        assertEquals("adminPage", view);
        assertEquals(1000L, modelMap.get("totalListening"));
        assertEquals(10.5, modelMap.get("listeningGrowth"));
        assertEquals(200L, modelMap.get("totalUsers"));
        assertEquals(5.0, modelMap.get("usersGrowth"));
        assertEquals(50L, modelMap.get("totalArtists"));
        assertEquals(3.2, modelMap.get("artistsGrowth"));
        assertEquals(topSongs, modelMap.get("songs"));
        verify(userService, times(6)).getAdminDashboardStats();
        verify(songService).getTopSongPopularityLastMonth(pageable);
    }

    @Test
    void home_zeroStats_modelContainsZeroValues() {
        Pageable pageable = PageRequest.of(0, 5);
        ModelMap modelMap = new ModelMap();
        Object stats = setupDashboardStats(0L, 0.0, 0L, 0.0, 0L, 0.0);
        Page<Object> emptyPage = new PageImpl<>(Collections.emptyList());

        doReturn(stats).when(userService).getAdminDashboardStats();
        doReturn(emptyPage).when(songService).getTopSongPopularityLastMonth(pageable);

        String view = adminController.home(modelMap, pageable);

        assertEquals("adminPage", view);
        assertEquals(0L, modelMap.get("totalListening"));
        assertEquals(0L, modelMap.get("totalUsers"));
        assertEquals(0L, modelMap.get("totalArtists"));
        assertEquals(emptyPage, modelMap.get("songs"));
    }

    @Test
    void home_songServiceCalledWithCorrectPageable() {
        Pageable pageable = PageRequest.of(0, 5);
        ModelMap modelMap = new ModelMap();
        Object stats = setupDashboardStats(0L, 0.0, 0L, 0.0, 0L, 0.0);

        doReturn(stats).when(userService).getAdminDashboardStats();
        doReturn(new PageImpl<>(Collections.emptyList())).when(songService).getTopSongPopularityLastMonth(pageable);

        adminController.home(modelMap, pageable);

        verify(songService).getTopSongPopularityLastMonth(eq(pageable));
    }

    @Test
    void users_validPageableAndCriteria_returnsUsersViewWithModel() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        UserSearchCriteria criteria = new UserSearchCriteria();
        Page<User> userPage = new PageImpl<>(List.of(new User()));
        ModelMap modelMap = new ModelMap();

        doReturn(userPage).when(userService).findUsersPage(pageable, criteria);

        String view = adminController.users(modelMap, pageable, criteria);

        assertEquals("users", view);
        assertEquals(userPage, modelMap.get("users"));
        assertEquals(criteria, modelMap.get("criteria"));
        verify(userService).findUsersPage(pageable, criteria);
    }

    @Test
    void users_emptyUserPage_modelContainsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        UserSearchCriteria criteria = new UserSearchCriteria();
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        ModelMap modelMap = new ModelMap();

        doReturn(emptyPage).when(userService).findUsersPage(pageable, criteria);

        String view = adminController.users(modelMap, pageable, criteria);

        assertEquals("users", view);
        assertEquals(emptyPage, modelMap.get("users"));
    }

    @Test
    void users_criteriaIsPreservedInModel() {
        Pageable pageable = PageRequest.of(0, 5);
        UserSearchCriteria criteria = new UserSearchCriteria();
        ModelMap modelMap = new ModelMap();

        doReturn(new PageImpl<>(Collections.emptyList())).when(userService).findUsersPage(pageable, criteria);

        adminController.users(modelMap, pageable, criteria);

        assertEquals(criteria, modelMap.get("criteria"));
    }

    @Test
    void users_serviceCalledWithCorrectArguments() {
        Pageable pageable = PageRequest.of(1, 5);
        UserSearchCriteria criteria = new UserSearchCriteria();
        ModelMap modelMap = new ModelMap();

        doReturn(new PageImpl<>(Collections.emptyList())).when(userService).findUsersPage(pageable, criteria);

        adminController.users(modelMap, pageable, criteria);

        verify(userService).findUsersPage(eq(pageable), eq(criteria));
    }

    @Test
    void updateUser_validUser_updatesAndRedirectsToAdminUsers() {
        User user = new User();

        String view = adminController.updateUser(user);

        assertEquals("redirect:/admin/users", view);
        verify(userService).update(user);
    }

    @Test
    void updateUser_serviceCalledWithCorrectUser() {
        User user = new User();

        adminController.updateUser(user);

        verify(userService).update(eq(user));
    }

    @SuppressWarnings("unchecked")
    private Object setupDashboardStats(long totalListening, double listeningGrowth,
                                       long totalUsers, double usersGrowth,
                                       long totalArtists, double artistsGrowth) {
        AdminDashboardStats stats = mock(AdminDashboardStats.class);
        when(stats.totalListening()).thenReturn(totalListening);
        when(stats.listeningGrowthPercent()).thenReturn(listeningGrowth);
        when(stats.totalUsers()).thenReturn(totalUsers);
        when(stats.usersGrowthPercent()).thenReturn(usersGrowth);
        when(stats.totalArtists()).thenReturn(totalArtists);
        when(stats.artistsGrowthPercent()).thenReturn(artistsGrowth);
        return stats;
    }
}