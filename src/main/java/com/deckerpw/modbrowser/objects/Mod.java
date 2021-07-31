package com.deckerpw.modbrowser.objects;


public class Mod {

    public int id;
    public String name;
    public String description;
    public String authors = "";


    public Mod(int id, String name){
        this.id = id;
        this.name = name;
    }

    public Mod(){ }

}
