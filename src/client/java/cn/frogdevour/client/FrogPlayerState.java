package cn.frogdevour.client;

import cn.frogdevour.GrowthRules.Stage;

public interface FrogPlayerState {
    Stage frogdevour$stage();
    void frogdevour$stage(Stage stage);
    boolean frogdevour$jumping();
    void frogdevour$jumping(boolean jumping);
}
