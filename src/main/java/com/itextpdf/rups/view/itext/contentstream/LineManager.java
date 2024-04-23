package com.itextpdf.rups.view.itext.contentstream;

/*
 * 
 * A class to track the line number in a content stream
 * 
 */

public class LineManager {
   
    private int lineNumber;
    private boolean numberLines;
    private int margin;
    
    {
        lineNumber = 0;
        margin = 10;
    }

     /**
     * Create a LineManager with lineNumber = 0, margin = 10, numberLines = true
     */
    public LineManager() {
        numberLines = true; 
    }

     /**
     * Create a LineManager allowing for switching off the printing (hack to pass tests)
     *
     * @param numLines boolean indicating whether to print line numbers
     */
    public LineManager(boolean numLines){
        numberLines = numLines;
    }

    /**
     * Get the current lineNumber as a padded string of length this.margin.
     *
     * @return the padded string
     */
    public String getLineNumberString(){

        if (numberLines) {
            return pad(String.valueOf(lineNumber));
        } else {
            return "";
        }
    }

    /**
     * Increase line number by 1
     */
    public void increaseLineNumber(){
        lineNumber++;
    }

    /**
     * Reset line number to zero.
     */
    public void reset() {
        lineNumber = 0;
    }

    /**
     * Get the current lineNumber as a padded string of length this.margin.
     *
     * @param str a line number string to be padded
     * @return the padded string
     */
    private String pad(String str) {

        return str + " ".repeat(margin - str.length());        

    }
}
