package com.athens.athensfx;

import java.util.function.Consumer;

public class Man extends Person {
    static Consumer<Man> single = Man::single;
    static Consumer<Man> old = (man) -> {};
    static Consumer<Man> dead = (man) -> {};
    static Consumer<Man> young = Person::tooYoung;
    private Consumer<Man> statusFunc = young;

    public Man(boolean horny, Population pop) {
        super(pop, (horny) ? pop.philanderers: pop.faithfulMen);
    }

    public void update(int i) throws InterruptedException {
        statusFunc.accept(this);
        deathChance(i);
    }

    void single() {
        Woman woman = p.getRandomWoman(); // !(horny && !woman.horny)
        if (woman.isSingle()) { woman.setPregnant(); }
        tooOld();
    }

    void die(int i) throws InterruptedException { p.menHolder.dead.put(i); }

    void setSingle() { statusFunc = single; }

    void setOld() { statusFunc = old; }

    void setDead() { statusFunc = dead; }

}
