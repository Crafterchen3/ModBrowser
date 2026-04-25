package com.deckerpw.modbrowser.data;

import net.minecraft.network.chat.Component;

public record Mod(String id,String iconUrl,String author, Component name, Component summary) {
}
