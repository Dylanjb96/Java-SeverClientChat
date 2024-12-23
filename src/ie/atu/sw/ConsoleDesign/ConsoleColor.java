package ie.atu.sw.ConsoleDesign;

/*
 * ANSI escape sequences are a standard for controlling cursor location, color, 
 * font styling, and other options on DOS, Mac and Linux terminals. The ANSI escape 
 * codes are formatted as follows:
 * 
 *  	[<PREFIX>];[<COLOR>];[<TEXT DECORATION>]
 *  
 *  See https://en.wikipedia.org/wiki/ANSI_escape_code for a decent description.
 */
public enum ConsoleColor {
    // Reset
    RESET("Reset", "0"),

    // Regular Colors
    BLACK("Black [Regular]", "0;30"),
    RED("Red [Regular]", "0;31"),
    GREEN("Green [Regular]", "0;32"),
    YELLOW("Yellow [Regular]", "0;33"),
    BLUE("Blue [Regular]", "0;34"),
    PURPLE("Purple [Regular]", "0;35"),
    CYAN("Cyan [Regular]", "0;36"),
    WHITE("White [Regular]", "0;37"),
    ORANGE("Orange [Regular]", "38;5;214"), // Custom color
    RED_WINE("Red Wine [Regular]", "38;5;52"), // Custom color
    DUSK("Dusk [Regular]", "38;5;240"), // Custom color
    SUNRISE("Sunrise [Regular]", "38;5;222"), // Custom color

    // Bold
    BLACK_BOLD("Black [Bold]", "1;30"),
    RED_BOLD("Red [Bold]", "1;31"),
    GREEN_BOLD("Green [Bold]", "1;32"),
    YELLOW_BOLD("Yellow [Bold]", "1;33"),
    BLUE_BOLD("Blue [Bold]", "1;34"),
    PURPLE_BOLD("Purple [Bold]", "1;35"),
    CYAN_BOLD("Cyan [Bold]", "1;36"),
    WHITE_BOLD("White [Bold]", "1;37"),
    ORANGE_BOLD("Orange [Bold]", "1;38;5;214"), // Custom bold color
    RED_WINE_BOLD("Red Wine [Bold]", "1;38;5;52"), // Custom bold color
    DUSK_BOLD("Dusk [Bold]", "1;38;5;240"), // Custom bold color
    SUNRISE_BOLD("Sunrise [Bold]", "1;38;5;222"), // Custom bold color

    // Underline
    BLACK_UNDERLINED("Black [Underline]", "4;30"),
    RED_UNDERLINED("Red [Underline]", "4;31"),
    GREEN_UNDERLINED("Green [Underline]", "4;32"),
    YELLOW_UNDERLINED("Yellow [Underline]", "4;33"),
    BLUE_UNDERLINED("Blue [Underline]", "4;34"),
    PURPLE_UNDERLINED("Purple [Underline]", "4;35"),
    CYAN_UNDERLINED("Cyan [Underline]", "4;36"),
    WHITE_UNDERLINED("White [Underline]", "4;37"),
    ORANGE_UNDERLINED("Orange [Underline]", "4;38;5;214"), // Custom underline color
    RED_WINE_UNDERLINED("Red Wine [Underline]", "4;38;5;52"), // Custom underline color
    DUSK_UNDERLINED("Dusk [Underline]", "4;38;5;240"), // Custom underline color
    SUNRISE_UNDERLINED("Sunrise [Underline]", "4;38;5;222"), // Custom underline color

    // Background
    BLACK_BACKGROUND("Black [Background]", "40"),
    RED_BACKGROUND("Red [Background]", "41"),
    GREEN_BACKGROUND("Green [Background]", "42"),
    YELLOW_BACKGROUND("Yellow [Background]", "43"),
    BLUE_BACKGROUND("Blue [Background]", "44"),
    PURPLE_BACKGROUND("Purple [Background]", "45"),
    CYAN_BACKGROUND("Cyan [Background]", "46"),
    WHITE_BACKGROUND("White [Background]", "47"),
    ORANGE_BACKGROUND("Orange [Background]", "48;5;214"), // Custom background color
    RED_WINE_BACKGROUND("Red Wine [Background]", "48;5;52"), // Custom background color
    DUSK_BACKGROUND("Dusk [Background]", "48;5;240"), // Custom background color
    SUNRISE_BACKGROUND("Sunrise [Background]", "48;5;222"), // Custom background color

    // High intensity
    BLACK_BRIGHT("Black [High Intensity]", "0;90"),
    RED_BRIGHT("Red [High Intensity]", "0;91"),
    GREEN_BRIGHT("Green [High Intensity]", "0;92"),
    YELLOW_BRIGHT("Yellow [High Intensity]", "0;93"),
    BLUE_BRIGHT("Blue [High Intensity]", "0;94"),
    PURPLE_BRIGHT("Purple [High Intensity]", "0;95"),
    CYAN_BRIGHT("Cyan [High Intensity]", "0;96"),
    WHITE_BRIGHT("White [High Intensity]", "0;97"),
    ORANGE_BRIGHT("Orange [High Intensity]", "0;38;5;214"), // Custom bright color
    RED_WINE_BRIGHT("Red Wine [High Intensity]", "0;38;5;52"), // Custom bright color
    DUSK_BRIGHT("Dusk [High Intensity]", "0;38;5;240"), // Custom bright color
    SUNRISE_BRIGHT("Sunrise [High Intensity]", "0;38;5;222"), // Custom bright color

    // Bold high intensity
    BLACK_BOLD_BRIGHT("Black [Bold High Intensity]", "1;90"),
    RED_BOLD_BRIGHT("Red [Bold High Intensity]", "1;91"),
    GREEN_BOLD_BRIGHT("Green [Bold High Intensity]", "1;92"),
    YELLOW_BOLD_BRIGHT("Yellow [Bold High Intensity]", "1;93"),
    BLUE_BOLD_BRIGHT("Blue [Bold High Intensity]", "1;94"),
    PURPLE_BOLD_BRIGHT("Purple [Bold High Intensity]", "1;95"),
    CYAN_BOLD_BRIGHT("Cyan [Bold High Intensity]", "1;96"),
    WHITE_BOLD_BRIGHT("White [Bold High Intensity]", "1;97"),
    ORANGE_BOLD_BRIGHT("Orange [Bold High Intensity]", "1;38;5;214"), // Custom bold high intensity color
    RED_WINE_BOLD_BRIGHT("Red Wine [Bold High Intensity]", "1;38;5;52"), // Custom bold high intensity color
    DUSK_BOLD_BRIGHT("Dusk [Bold High Intensity]", "1;38;5;240"), // Custom bold high intensity color
    SUNRISE_BOLD_BRIGHT("Sunrise [Bold High Intensity]", "1;38;5;222"), // Custom bold high intensity color

    // High Intensity backgrounds
    BLACK_BACKGROUND_BRIGHT("Black [High Intensity BG]", "0;100"),
    RED_BACKGROUND_BRIGHT("Red [High Intensity BG]", "0;101"),
    GREEN_BACKGROUND_BRIGHT("Green [High Intensity BG]", "0;102"),
    YELLOW_BACKGROUND_BRIGHT("Yellow [High Intensity BG]", "0;103"),
    BLUE_BACKGROUND_BRIGHT("Blue [High Intensity BG]", "0;104"),
    PURPLE_BACKGROUND_BRIGHT("Purple [High Intensity BG]", "0;105"),
    CYAN_BACKGROUND_BRIGHT("Cyan [High Intensity BG]", "0;106"),
    WHITE_BACKGROUND_BRIGHT("White [High Intensity BG]", "0;107"),
    ORANGE_BACKGROUND_BRIGHT("Orange [High Intensity BG]", "0;48;5;214"), // Custom high intensity background color
    RED_WINE_BACKGROUND_BRIGHT("Red Wine [High Intensity BG]", "0;48;5;52"), // Custom high intensity background color
    DUSK_BACKGROUND_BRIGHT("Dusk [High Intensity BG]", "0;48;5;240"), // Custom high intensity background color
    SUNRISE_BACKGROUND_BRIGHT("Sunrise [High Intensity BG]", "0;48;5;222"); // Custom high intensity background color

    // Control Sequence Introducer. ASCII Octal = \033, ASCII Hex = \0x1B, Shell =
    // \e
    private static final String CTRL_SEQ_INTRO = "\033[";
    private static final String CTRL_SEQ_END = "m"; // Terminates control
    private final String description; // Description of the console color
    private final String color; // ANSI escape code for the console color

    /**
     * Constructor for ConsoleColor enum constants.
     *
     * @param description the description or name of the console color
     * @param color       the ANSI escape code representing the console color
     */
    ConsoleColor(String description, String color) {
        this.description = description;
        this.color = color;
    }

    /**
     * Retrieves the description or name of the console color.
     *
     * @return the description of the console color
     */
    public String description() {
        return this.description;
    }

    /**
     * Retrieves the ANSI escape code representing the console color.
     *
     * @return the ANSI escape code for the console color
     */
    public String color() {
        return toString();
    }

    /**
     * Converts the ConsoleColor enum constant to its ANSI escape sequence
     * representation.
     *
     * @return the ANSI escape sequence for the console color
     */
    @Override
    public String toString() {
        return CTRL_SEQ_INTRO + this.color + CTRL_SEQ_END;
    }
}