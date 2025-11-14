/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author danie
 */
public interface IGameFunction {
    public void iniciar();
    public Stat getStats();
    void setGameListener(IGameListener listener);

    
}  
