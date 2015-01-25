/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voiceinteraction;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.irit.elipse.enseignement.isia.PaletteGraphique;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sacapuce
 */
public class Commands {

    //private static boolean isThereAHergerFrame = false;
    private static boolean isThereAPaletteFrame = false;
    private static boolean isThereAIcarFrame = false;
    private static final String address = "127.255.255:2010";
    private static Timer timer = new Timer();

    public static void commandsGeneration(IvyClient ic, String[] strings,
            ArrayList<String> commands, Ivy bus) {
        if (findWord(strings, "initialiser")) {
            // Initiliaze Palette
            if (!isThereAPaletteFrame && findWord(strings, "palette")) {
                PaletteGraphique myPG = new PaletteGraphique(address, 0, 0, 300, 300);
                isThereAPaletteFrame = true;
            }

            // Initialize Icar
            if (!isThereAIcarFrame && findWord(strings, "icar")) {
                // Not the proper component to initialize, nothing happens
                //icar.IcarComponent myIcar = new icar.IcarComponent(address, 100, 100);
                isThereAIcarFrame = true;
            }

            // Initialize Herger
            /*if (!isThereAHergerFrame && findWord(strings, "herger")) {
             HergerUI myHerger = new HergerUI(address, 0, 0, 300, 300);
             isThereAHergerFrame = true;
             }*/
        }

        // Get a creation
        if (findWord(strings, "creation")) {
            // A creation is the first command
            executeCommands(commands, bus);
            restartTimer(commands, bus);

            if (findWord(strings, "rectangle")) {
                commands.add("creation rectangle");
            } else if (findWord(strings, "ellipse")) {
                commands.add("creation ellipse");
            }
        }

        // Get a moving
        if (findWord(strings, "deplacement")) {
            // A creation is the first command
            executeCommands(commands, bus);
            restartTimer(commands, bus);

            if (findWord(strings, "rectangle")) {
                commands.add("deplacement rectangle");
            } else if (findWord(strings, "ellipse")) {
                commands.add("deplacement ellipse");
            }
        }

        // Get a color
        if (findWord(strings, "Couleur")) {
            restartTimer(commands, bus);

            if (findWord(strings, "vert")) {
                commands.add("couleur vert");
            } else if (findWord(strings, "rouge")) {
                commands.add("couleur rouge");
            } else if (findWord(strings, "bleu")) {
                commands.add("couleur bleu");
            }
        }

        // Get a place
        if (findWord(strings, "Emplacement")) {
            restartTimer(commands, bus);

            if (findWord(strings, "ici")) {
                commands.add("emplacement ici");
            }
        }

        // Get a mouse click
        if (findWord(strings, "MousePressed")) {
            restartTimer(commands, bus);

            int x = 0, y = 0;

            // Get numbers and equal signs
            String copy = strings[0].replaceAll("[([a-z]|[A-Z]|:| )]", "");
            // Get numbers
            String[] numbers = copy.split("=");
            x = Integer.parseInt(numbers[1]);
            y = Integer.parseInt(numbers[2]);

            commands.add("mousepressed");
            commands.add(Integer.toString(x));
            commands.add(Integer.toString(y));
        }

        // Get a clean
        if (findWord(strings, "nettoyer") && findWord(strings, "commandes")) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
            executeCommands(commands, bus);
        }
    }

    public static void restartTimer(ArrayList<String> commands, Ivy bus) {
        timer.cancel();
        timer.purge();
        timer = new Timer();
        setTimerTask(commands, bus);
    }

    public static void setTimerTask(ArrayList<String> commands, Ivy bus) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                executeCommands(commands, bus);
            }
        }, 2000);
    }

    /**
     * Put all stored instructions into a single String and send it when there
     * is no more commands stored, then clear the command storage
     *
     * @param commands
     * @param bus
     */
    public static void executeCommands(ArrayList<String> commands, Ivy bus) {
        System.out.print("DEBUG > inside commands ");
        commands.stream().forEach((command) -> {
            System.out.print(" - " + command);
        });
        System.out.println();

        String instruction = "";
        int stage = 0;

        if (commands.isEmpty()) {
            return;
        }

        // First stage : creation, moving or modify
        if (findWord(commands.get(stage), "creation")
                || findWord(commands.get(stage), "deplacer ce")
                || findWord(commands.get(stage), "modifier ce")) {
            if (findWord(commands.get(stage), "creation")) {
                // Creation of a rectangle
                if (findWord(commands.get(stage), "rectangle")) {
                    instruction = instruction.concat("Palette:CreerRectangle ");
                }
                if (findWord(commands.get(stage), "ellipse")) {
                    instruction = instruction.concat("Palette:CreerEllipse ");
                }
            }

            // First stage : moving
            if (findWord(commands.get(stage), "deplacement")) {
                // Creation of a rectangle
                if (findWord(commands.get(stage), "rectangle")) {
                    instruction = instruction.concat("Palette:DeplacerObjet ");
                }
                if (findWord(commands.get(stage), "ellipse")) {
                    instruction = instruction.concat("Palette:DeplacerObjet ");
                }
            }

            stage++;
        }

        // Second stage : designation
        /*if (commands.size() > stage) {
            if ((commands.size() > stage + 3)
                    && (findWord(commands.get(stage + 1), "mousepressed"))) {
                stage++;

                // x and y are in a different String than mousepressed, easier to store and take back
                instruction = instruction.concat("x=" + commands.get(stage + 1)
                        + " y=" + commands.get(stage + 2) + " ");

                stage = stage + 2;
            }
            stage++;
        }*/

        // Second stage or fourth : place
        if (commands.size() > stage) {
            if (findWord(commands.get(stage), "ici")) {
                // No treatment for that instruction but click should follow
                if ((commands.size() > stage + 3)
                        && (findWord(commands.get(stage + 1), "mousepressed"))) {
                    stage++;

                    // x and y are in a different String than mousepressed, easier to store and take back
                    instruction = instruction.concat("x=" + commands.get(stage + 1)
                            + " y=" + commands.get(stage + 2) + " ");

                    stage = stage + 2;
                }
                stage++;
            }
        }

        // Second or third stage : color
        if (commands.size() > stage) {
            // Color
            if (findWord(commands.get(stage), "couleur")) {
                if (findWord(commands.get(stage), "rouge")) {
                    instruction = instruction.concat("couleurFond=255:0:0 couleurContour=255:0:0 ");
                }
                if (findWord(commands.get(stage), "vert")) {
                    instruction = instruction.concat("couleurFond=0:255:0 couleurContour=0:255:0 ");
                }
                if (findWord(commands.get(stage), "bleu")) {
                    instruction = instruction.concat("couleurFond=0:0:255 couleurContour=0:0:255 ");
                }
                stage++;
            }
        }

        try {
            bus.sendMsg(instruction);
        } catch (IvyException ex) {
            Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
        }
        commands.clear();
    }

    public static boolean findWord(String string, String word) {
        return string.contains(word);
    }

    public static boolean findWord(String[] strings, String word) {
        for (String string : strings) {
            if (string.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
