package edu.spbstu.menu;

import java.io.IOException;

public class Menu {
    private static final String MAIN_MENU =
            """
                    Главное меню:
                    1. Загрузка данных
                    2. Выбор алгоритма
                    3. Получение результата
                    4. Получение справки
                    5. Выход""";
    private static final String INPUT_CHOICE = """
            Выберите способ ввода данных:
            1. Ввести данные вручную
            2. Загрузить данные из файла
            3. Ввести данные в формате JSON""";
    private static final String ALGORITHM_CHOICE = """
            Выберите алгоритм:
            1. DSATUR
            2. RFL""";
    public static final String OUTPUT_CHOICE = """
            Выберите способ вывода данных:
            1. Вывод в консоль
            2. Вывод JSON
            3. Вывод в JSON файл
            4. Рекомендации""";

    private final IOAdapter ioAdapter;

    public Menu(IOAdapter ioAdapter) {
        this.ioAdapter = ioAdapter;
    }

    public MainMenuChoice displayMain() throws IOException {
        ioAdapter.printLine(MAIN_MENU);
        return MainMenuChoice.values()[getChoice(MainMenuChoice.values().length) - 1];
    }

    public void displayHelp(HelpInfoProvider helpInfoProvider) throws IOException {
        ioAdapter.printLine(helpInfoProvider.getHelpInfo());
    }

    public void displayRecommendations(String[] recommendations) throws IOException {
        ioAdapter.printLine("Рекомендации:");
        for (String recommendation : recommendations) {
            ioAdapter.printLine("- " + recommendation);
        }
        ioAdapter.readLine();
    }

    public int getChoice(int max) throws IOException {
        boolean validInput = false;
        int choice = -1;
        do {
            String input = ioAdapter.readLine();
            try {
                choice = getChoiceOnce(input, max);
                validInput = choice != -1;
            } catch (NumberFormatException e) {
                ioAdapter.printLine("Некорректный ввод. Пожалуйста, введите число от 1 до " + max + ".");
            }
        } while (!validInput);
        return choice;
    }

    public int getChoiceOnce(String input, int max) throws NumberFormatException, IOException {
        if (input == null) {
            return -1;
        }
        if (input.toLowerCase().contains("valid")) return 101; //FIXME BUG
        int choice;
        choice = Integer.parseInt(input);
        if (choice >= 1 && choice <= max) {
            return choice;
        } else {
            choice = -1;
            ioAdapter.printLine("Пожалуйста, введите число от 1 до " + max + ".");
        }
        return choice;
    }

    public InputChoice getInputChoice() throws IOException {
        ioAdapter.printLine(INPUT_CHOICE);
        return InputChoice.values()[getChoice(InputChoice.values().length) - 1];
    }

    public AlgorithmChoice getAlgorithmChoice() throws IOException {
        ioAdapter.printLine(ALGORITHM_CHOICE);
        return AlgorithmChoice.values()[getChoice(AlgorithmChoice.values().length) - 1];
    }

    public OutputChoice getOutputChoice() throws IOException {
        ioAdapter.printLine(OUTPUT_CHOICE);
        return OutputChoice.values()[getChoice(OutputChoice.values().length) - 1];
    }

    public enum MainMenuChoice {
        INPUT,
        ALGORITHM,
        OUTPUT,
        HELP,
        EXIT
    }

    public enum InputChoice {
        MANUAL,
        JSON_FILE,
        JSON
    }

    public enum AlgorithmChoice {
        DSATUR,
        RFL
    }

    public enum OutputChoice {
        CONSOLE,
        JSON_CONSOLE,
        JSON_FILE,
        RECOMMENDATIONS
    }
}
