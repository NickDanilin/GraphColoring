package edu.spbstu;

import edu.spbstu.coloring.ColoredVertex;
import edu.spbstu.coloring.ColoringAlgorithm;
import edu.spbstu.coloring.DSaturColoring;
import edu.spbstu.input.InputConverterUtility;
import edu.spbstu.input.InputValidationException;
import edu.spbstu.menu.HelpInfoProvider;
import edu.spbstu.menu.IOAdapter;
import edu.spbstu.menu.Menu;
import edu.spbstu.output.OutputConverterUtility;
import edu.spbstu.output.RecommendationUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {
    private final IOAdapter ioAdapter;
    private final Menu menu;
    private final HelpInfoProvider helpInfoProvider;

    private List<int[]> adjacencyList = null;
    private Map<ColoredVertex, int[]> coloringResult = null;

    private static final ColoringAlgorithm DSATUR_ALGORITHM = new DSaturColoring();
    private static final ColoringAlgorithm RFL_ALGORITHM = new DSaturColoring();

    public App(
            IOAdapter ioAdapter,
            HelpInfoProvider helpInfoProvider
    ) {
        this.ioAdapter = ioAdapter;
        this.menu = new Menu(ioAdapter);
        this.helpInfoProvider = helpInfoProvider;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                Menu.MainMenuChoice mainMenuChoice = menu.displayMain();
                switch (mainMenuChoice) {
                    case INPUT -> inputStep();
                    case ALGORITHM -> {
                        if (adjacencyList == null) {
                            ioAdapter.printLine("Сначала загрузите данные.");
                            break;
                        }
                        algorithmStep();
                    }
                    case OUTPUT -> {
                        if (coloringResult == null) {
                            ioAdapter.printLine("Сначала выполните алгоритм.");
                            break;
                        }
                        outputStep();
                    }
                    case HELP -> menu.displayHelp(helpInfoProvider);
                    case EXIT -> running = false;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void outputStep() throws IOException {
        Menu.OutputChoice outputChoice = menu.getOutputChoice();
        ioAdapter.printLine("Результат раскраски графа:");
        String output = "";
        switch (outputChoice) {
            case CONSOLE -> output = OutputConverterUtility.convertToString(coloringResult);
            case JSON_CONSOLE -> output = OutputConverterUtility.convertToJson(coloringResult);
            case JSON_FILE -> {
                ioAdapter.printLine("Введите путь для сохранения JSON файла:");
                String filePath = ioAdapter.readLine().strip();
                if (filePath.startsWith("\"") && filePath.endsWith("\"")) {
                    filePath = filePath.substring(1, filePath.length() - 1);
                }
                Path path = Path.of(filePath);
                try {
                    Files.writeString(path, OutputConverterUtility.convertToJson(coloringResult));
                    output = "Результат успешно сохранен в файл: " + filePath;
                } catch (IOException e) {
                    output = "Ошибка при записи в файл: " + e.getMessage();
                }
            }
            case RECOMMENDATIONS -> {
                menu.displayRecommendations(RecommendationUtility.getRecommendation(coloringResult));
            }
        }
        ioAdapter.printLine(output);
    }

    private void algorithmStep() throws IOException {
        Menu.AlgorithmChoice algorithmChoice = menu.getAlgorithmChoice();
        long startTime = System.nanoTime();
        switch (algorithmChoice) {
            case DSATUR -> {
                ioAdapter.printLine("Запуск алгоритма DSATUR...");
                startTime = System.nanoTime();
                coloringResult = DSATUR_ALGORITHM.colorGraph(adjacencyList);
            }
            case RFL -> {
                ioAdapter.printLine("Запуск алгоритма RFL...");
                startTime = System.nanoTime();
                coloringResult = RFL_ALGORITHM.colorGraph(adjacencyList);
            }
        }
        long endTime = System.nanoTime();
        long elapsedTime = (endTime - startTime) / 1_000_000;
        ioAdapter.printLine("Выполнение завершено. Время выполнения: " + elapsedTime + " мс");

    }

    private void inputStep() throws IOException {
        Menu.InputChoice inputChoice = menu.getInputChoice();
        switch (inputChoice) {
            case MANUAL -> {
                ioAdapter.printLine("Введите количество вершин:");
                String input = ioAdapter.readLine();
                if (!input.matches("\\d+")) {
                    ioAdapter.printLine("Некорректный ввод. Пожалуйста, введите число.");
                    ioAdapter.readLine();
                    break;
                }
                int verticesCount = Integer.parseInt(input);
                if (verticesCount <= 0) {
                    ioAdapter.printLine("Количество вершин должно быть больше нуля.");
                    ioAdapter.readLine();
                    break;
                }

                List<String> userLines = new ArrayList<>();
                for (int i = 0; i < verticesCount; i++) {
                    ioAdapter.printLine("Введите смежные вершины для вершины " + (i + 1) + " (через пробел):");
                    userLines.add(ioAdapter.readLine());
                }

                try {
                    adjacencyList = InputConverterUtility.convertFromUserInput(userLines, verticesCount);
                } catch (InputValidationException e) {
                    ioAdapter.printLine("Ошибка валидации ввода: " + e.getMessage());
                }
            }
            case JSON_FILE -> {
                ioAdapter.printLine("Введите путь к JSON файлу:");
                String filePath = ioAdapter.readLine().strip();
                if (filePath.startsWith("\"") && filePath.endsWith("\"")) {
                    filePath = filePath.substring(1, filePath.length() - 1);
                }
                Path path = Path.of(filePath);
                String json;
                try {
                    if (!Files.exists(path)) {
                        ioAdapter.printLine("Файл не найден. Пожалуйста, проверьте путь.");
                        break;
                    }
                    json = Files.readString(path);
                } catch (Exception e) {
                    ioAdapter.printLine("Ошибка доступа к файлу: " + e.getMessage());
                    break;
                }

                try {
                    adjacencyList = InputConverterUtility.convertFromJson(json);
                } catch (InputValidationException e) {
                    ioAdapter.printLine("Ошибка валидации JSON: " + e.getMessage());
                }
            }
            case JSON -> {
                ioAdapter.printLine("Введите JSON строку:");
                String json = ioAdapter.readLine();
                try {
                    adjacencyList = InputConverterUtility.convertFromJson(json);
                } catch (InputValidationException e) {
                    ioAdapter.printLine("Ошибка валидации JSON: " + e.getMessage());
                }
            }
        }
    }

}
