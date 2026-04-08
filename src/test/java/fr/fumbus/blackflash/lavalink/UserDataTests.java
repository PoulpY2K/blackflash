package fr.fumbus.blackflash.lavalink;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDataTests {

    @Test
    void userdata_storesRequesterIdCorrectly() {
        assertThat(new UserData(789L).requester()).isEqualTo(789L);
    }

    @Test
    void userdata_instancesWithSameIdAreEqualAndHaveSameHashCode() {
        UserData a = new UserData(42L);
        UserData b = new UserData(42L);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void userdata_twoInstancesWithDifferentIdsAreNotEqual() {
        assertThat(new UserData(1L)).isNotEqualTo(new UserData(2L));
    }
}
