package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String text =  getTextFromFile(args[0]);
        System.out.println("The text is:");
        System.out.println(text);
        System.out.println();

        int[] breakdown = breakdownText(text);

        System.out.println("Words: " + breakdown[0]);
        System.out.println("Sentences: " + breakdown[1]);
        System.out.println("Characters: " + breakdown[2]);
        System.out.println("Syllables: " + breakdown[3]);
        System.out.println("Polysyllables: " + breakdown[4]);
        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.next().toLowerCase();

        double clScore, fkScore, smogScore, ariScore;
        int age = 0;
        double averageAge = 0.0;

        System.out.println();
        if (choice.equals("ari") || choice.equals("all")) {
            System.out.print("Automated Readability Index: ");
            ariScore = getAriScore(breakdown);
            System.out.printf("%.2f", ariScore);
            age = getAgeRange(ariScore);
            averageAge += age;
            System.out.println(" (about " + age + "-year-olds).");
        }
        if (choice.equals("fk") || choice.equals("all")) {
            System.out.print("Flesch–Kincaid readability tests: ");
            fkScore = getFkScore(breakdown);
            System.out.printf("%.2f", fkScore);
            age = getAgeRange(fkScore);
            averageAge += age;
            System.out.println(" (about " + age + "-year-olds).");
        }
        if (choice.equals("smog") || choice.equals("all")) {
            System.out.print("Simple Measure of Gobbledygook: ");
            smogScore = getSmogScore(breakdown);
            System.out.printf("%.2f", smogScore);
            age = getAgeRange(smogScore);
            averageAge += age;
            System.out.println(" (about " + age + "-year-olds).");
        }
        if (choice.equals("cl") || choice.equals("all")) {
            System.out.print("Coleman–Liau index: ");
            clScore = getClScore(breakdown);
            System.out.printf("%.2f", clScore);
            age = getAgeRange(clScore);
            averageAge += age;
            System.out.println(" (about " + age + "-year-olds).");
        }

        if (choice.equals("all")) averageAge /= 4;
        System.out.print("\nThis text should be understood in average by ");
        System.out.printf("%.2f",averageAge);
        System.out.println("-year-olds.");

    }

    public static int countSyllables(String word) {
        word = word.toLowerCase();
        Pattern vowelPattern = Pattern.compile("[aeiouy]+");
        Matcher match = vowelPattern.matcher(word);

        int syllables = 0;
        while (match.find()) syllables++;

        if (word.endsWith("e")) syllables--;
        return Math.max(syllables, 1);
    }

    public static int getAgeRange(double score) {
        return scoreGrade.getAgeFromScore((int) Math.ceil(score));
    }

    public static double getFkScore(int[] breakdown) {
        double w = breakdown[0];
        double s = breakdown[1];
        double sy = breakdown[3];
        return 0.39 * (w/s) + 11.8 * (sy/w) - 15.59;
    }

    public static double getAriScore(int[] breakdown) {
        double w = breakdown[0];
        double s = breakdown[1];
        double c = breakdown[2];
        return 4.71 * (c/w) + 0.5 * (w/s) - 21.43;
    }

    public static double getSmogScore(int[] breakdown) {
        double s = breakdown[1];
        double sy = breakdown[4];
        return 1.043 * Math.sqrt(sy*30/s) + 3.1291;
    }

    public static double getClScore(int[] breakdown) {
        double w = breakdown[0];
        double s = breakdown[1];
        double c = breakdown[2];
        return 0.0588 * (100 * c/w) - 0.296 * (100 * s/w) - 15.8;
    }

    public static int[] breakdownText(String text) {
       int wordCount = 1;
       int charCount = 0;
       int sentenceCount = 0;
       int syllableCount = 0;
       int pollyCount = 0;

        String[] words = text.split(" ");
        wordCount = words.length;

        for (String word : words) {
            word.replace("[\\n]||[\\t]", "");
            if (word.matches(".*[.!?]$")) sentenceCount++;
            charCount += word.length();
            int syllables = countSyllables(word);
            syllableCount += syllables;
            if (syllables > 2) pollyCount++;
        }

        if (!words[words.length - 1].matches(".*[.!?]$")) sentenceCount++;

       return new int[]{wordCount, sentenceCount, charCount, syllableCount, pollyCount};
    }

    public static String getTextFromFile(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            System.err.println("Failed to retrieve file: " +  fileName);
            throw new RuntimeException(e);
        }
    }

    public enum scoreGrade {
        KINDERGARTEN(1, 6),
        FIRST_GRADE(2, 7),
        SECOND_GRADE(3, 8),
        THIRD_GRADE(4, 9),
        FOURTH_GRADE(5, 10),
        FIFTH_GRADE(6, 11),
        SIXTH_GRADE(7, 12),
        SEVENTH_GRADE(8, 13),
        EIGHTH_GRADE(9, 14),
        NINTH_GRADE(10, 15),
        TENTH_GRADE(11, 16),
        ELEVENTH_GRADE(12, 17),
        TWELFTH_GRADE(13, 18),
        COLLEGE_STUDENT(14, 19);

        final int score;
        final int age;

        scoreGrade(int score, int age) {
            this.score = score;
            this.age = age;
        }

        public static int getAgeFromScore(int score) {
            for (scoreGrade level : scoreGrade.values()) {
                if (level.score == score) {
                    return level.age;
                }
            }
            return 22;
        }

        @Override
        public String toString() {
            StringBuilder name = new StringBuilder();
            for (String word : name().toLowerCase().split(" ")) {
                name.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
            }
            name.deleteCharAt(name.length() - 1);
            return name.toString();
        }
    }
}
