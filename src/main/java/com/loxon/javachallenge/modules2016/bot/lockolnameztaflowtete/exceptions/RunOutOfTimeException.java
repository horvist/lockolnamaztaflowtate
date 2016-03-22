package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions;

/**
 * @author kalmarr
 */
public class RunOutOfTimeException extends EndOfTurnException {

    public RunOutOfTimeException(String msg){
        super(msg);
    }
}
