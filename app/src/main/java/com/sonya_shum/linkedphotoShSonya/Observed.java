package com.sonya_shum.linkedphotoShSonya;

public interface Observed {//сущность котору наблюдаем
    public void addObserver(Observer observer);
    public void notifyObservers();//уведомлять наблюдателей
}
