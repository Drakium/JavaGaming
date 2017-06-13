package a2;

import sage.event.*;
public class CrashEvent extends AbstractGameEvent
{
 // programmer-defined parts go here
 private int whichCrash;
 public CrashEvent(int n) { whichCrash = n; }
 public int getWhichCrash() { return whichCrash; }
}