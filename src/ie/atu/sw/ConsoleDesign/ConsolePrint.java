package ie.atu.sw.ConsoleDesign;

/**
 * Utility class with static methods for consistent message printing.
 */
public class ConsolePrint {
    /**
     * Print a stylized title to the terminal.
     * 
     * @param title - A string to be stylized as a title
     */
    public static void printTitle(String title) {
        System.out.print(ConsoleColor.BLACK_BACKGROUND); // Set black background
        System.out.print(ConsoleColor.BLUE_BOLD); // Set bold blue text
        System.out.println(); // Print empty lines for spacing
        System.out.println();
        System.out.print("                         [TITLE] \n" + title); // Print stylized title with a leading label
        System.out.println();
        System.out.print(ConsoleColor.RESET); // Reset console colors
    }

    /**
     * Print a stylized option list to the terminal.
     * 
     * @param options - A list of options to be stylized as an options menu
     */
    public static void printMenuOptions(String[] options) {
        System.out.print(ConsoleColor.BLACK_BACKGROUND); // Set black background
        System.out.print(ConsoleColor.CYAN_BOLD_BRIGHT); // Set bright cyan bold text
        System.out.print("Please select the following: \n[OPTION]"); // Placeholder for options label
        System.out.println();

        // Print each option in list
        for (String option : options)
            System.out.println(option);

        System.out.println();
        System.out.print("You Chose Option: "); // Prompt user to choose an option
        System.out.print(ConsoleColor.RESET); // Reset console colors
    }

    /**
     * print a stylized title to the terminal
     * 
     * @param setting - a string to be stylized as a setting
     */
    public static void printConfigure(String setting) {
        System.out.print(ConsoleColor.BLACK_BACKGROUND); // Set black background
        System.out.print(ConsoleColor.SUNRISE_BOLD); // Set Sunrise colour text
        System.out.println();
        System.out.println();
        System.out.print("                         [CONFIGURATIONS] \n" + setting); // Configurations Title
        System.out.println();
        System.out.print(ConsoleColor.RESET); // Reset console colors
    }

    /**
     * print a stylized title to the terminal
     * 
     * @param section - a string to be stylized as a title
     */
    public static void printSection(String section) {
        System.out.print(ConsoleColor.BLACK_BACKGROUND); // Set black background
        System.out.print(ConsoleColor.ORANGE_BOLD); // Set orange colour text
        System.out.println();
        System.out.println();
        System.out.print("                         [SECTION] \n" + section); // Section Title
        System.out.println();
        System.out.print(ConsoleColor.RESET); // Reset console colors
    }

    /**
     * Print a stylized heading to the terminal.
     * 
     * @param heading - A string to be stylized as a heading
     */
    public static void printHeading(String heading) {
        // Print an empty line for spacing
        System.out.println();

        // Print the heading with an underline
        printWithUnderline(heading);

        // Print an empty line after the heading
        System.out.println();
    }

    /**
     * Print an underlined string to the terminal.
     * 
     * @param text - The string to be underlined
     */
    public static void printWithUnderline(String text) {
        // Set the console text color to black background
        System.out.print(ConsoleColor.BLACK_BACKGROUND);

        // Set the console text color to yellow with underline
        System.out.print(ConsoleColor.YELLOW_UNDERLINED);

        // Print the text with the specified styling
        System.out.print(text);

        // Reset the console text color to default
        System.out.print(ConsoleColor.RESET);
    }

    /**
     * Print stylized information-text to the terminal.
     * 
     * @param info - The string to be stylized as information-text
     */
    public static void printInfo(String info) {
        // Move to a new line before printing the information-text
        System.out.println();

        // Set the console text color to black background
        System.out.print(ConsoleColor.BLACK_BACKGROUND);

        // Set the console text color to bright green with bold
        System.out.print(ConsoleColor.GREEN_BOLD_BRIGHT);

        // Print the information-text with the specified styling
        System.out.print("[MSG] " + info);

        // Reset the console text color to default
        System.out.print(ConsoleColor.RESET);

        // Move to a new line after printing the information-text
        System.out.println();
    }

    /**
     * Print stylized warning-text to the terminal.
     * 
     * @param warning - The string to be stylized as warning-text
     */
    public static void printWarning(String warning) {
        // Move to a new line before printing the warning-text
        System.out.println();

        // Set the console text color to black background
        System.out.print(ConsoleColor.BLACK_BACKGROUND);

        // Set the console text color to bright yellow with bold
        System.out.print(ConsoleColor.YELLOW_BOLD_BRIGHT);

        // Print the warning-text with the specified styling
        System.out.print("[WARNING] " + warning);

        // Reset the console text color to default
        System.out.print(ConsoleColor.RESET);

        // Move to a new line after printing the warning-text
        System.out.println();
    }

    /**
     * Print stylized error-text to the terminal.
     * 
     * @param error - The string to be stylized as error-text
     */
    public static void printError(String error) {
        // Move to a new line before printing the error-text
        System.err.println();

        // Set the console text color to black background
        System.err.print(ConsoleColor.BLACK_BACKGROUND);

        // Set the console text color to bright red with bold
        System.err.print(ConsoleColor.RED_BOLD_BRIGHT);

        // Print the error-text with the specified styling
        System.err.print("[ERROR] " + error);

        // Reset the console text color to default
        System.err.print(ConsoleColor.RESET);

        // Move to a new line after printing the error-text
        System.err.println();
    }
}
