/*
 * Copyright (C) 2015 Sandra Bardot Jonas Gouraud
 * 
 * This file is part of FusionEngine.
 *
 * FusionEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FusionEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FusionEngine. If not, see <http://www.gnu.org/licenses/>.
 */

package recogestureherger;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 *
 * @author Jonas Gouraud, Sandra Bardot
 */
public class OneDollarRecognizer {

    // Recognizer class constants
    int numTemplates = 16;
    int numPoints = 64;
    double squareSize = 250.0;
    double halfDiagonal = 0.5 * Math.sqrt(250.0 * 250.0 + 250.0 * 250.0);
    double angleRange = 45.0;
    double anglePrecision = 2.0;
    double Phi = 0.5 * (-1.0 + Math.sqrt(5.0)); // Golden Ratio

    Recognizer recognizer;
    Result result = null;

    void setup() {
        recognizer = new Recognizer();
    }

    private double pathLength(ArrayList<Point> points) {
        double d = 0.0;

        for (int i = 1; i < points.size(); i++) {
            d += points.get(i - 1).distance(points.get(i));
        }
        return d;
    }

    private ArrayList<Point> resample(ArrayList<Point> points, int n) {
        int I = (int) (pathLength(points) / ((float) n - 1.0));
        double D = 0.0;
        ArrayList<Point> newpoints = new ArrayList<>();
        Stack stack = new Stack();
        for (int i = 0; i < points.size(); i++) {
            stack.push(points.get(points.size() - 1 - i));
        }

        while (!stack.empty()) {
            Point pt1 = (Point) stack.pop();

            if (stack.empty()) {
                newpoints.add(pt1);
                continue;
            }
            Point pt2 = (Point) stack.peek();
            double d = pt1.distance(pt2);
            if ((D + d) >= I) {
                int qx = (int) (pt1.x + ((I - D) / d) * (pt2.x - pt1.x));
                int qy = (int) (pt1.y + ((I - D) / d) * (pt2.y - pt1.y));
                Point q = new Point(qx, qy);
                newpoints.add(q);
                stack.push(q);
                D = 0.0;
            } else {
                D += d;
            }
        }

        if (newpoints.size() == (n - 1)) {
            newpoints.add(points.get(points.size() - 1));
        }
        return newpoints;

    }

    // A template holds a name and a set of reduced points that represent
    // a single gesture. 
    class Template {

        String Name;
        ArrayList<Point> Points;

        Template(String name, ArrayList<Point> points) {
            Name = name;
            Points = resample(points, numPoints);
            Points = rotateToZero(Points);
            Points = scaleToSquare(Points, squareSize);
            Points = translateToOrigin(Points);
        }
    }

    class Result {

        String Name;
        double Score;
        double Ratio;

        Result(String name, double score, double ratio) {
            Name = name;
            Score = score;
            Ratio = ratio;
        }
    }

    class Recognizer {

        ArrayList<Template> templates = new ArrayList<>();

        Recognizer() {

            // These predefines come from the sample code, and can be replaced or revised.
            // it is left as an exercise for the reader to implement a file format for reading
            // and saving templates.
            // triangle
            ArrayList<Point> point0 = new ArrayList(Arrays.asList(new Point(137, 139), new Point(135, 141), new Point(133, 144), new Point(132, 146),
                    new Point(130, 149), new Point(128, 151), new Point(126, 155), new Point(123, 160),
                    new Point(120, 166), new Point(116, 171), new Point(112, 177), new Point(107, 183),
                    new Point(102, 188), new Point(100, 191), new Point(95, 195), new Point(90, 199),
                    new Point(86, 203), new Point(82, 206), new Point(80, 209), new Point(75, 213),
                    new Point(73, 213), new Point(70, 216), new Point(67, 219), new Point(64, 221),
                    new Point(61, 223), new Point(60, 225), new Point(62, 226), new Point(65, 225),
                    new Point(67, 226), new Point(74, 226), new Point(77, 227), new Point(85, 229),
                    new Point(91, 230), new Point(99, 231), new Point(108, 232), new Point(116, 233),
                    new Point(125, 233), new Point(134, 234), new Point(145, 233), new Point(153, 232),
                    new Point(160, 233), new Point(170, 234), new Point(177, 235), new Point(179, 236),
                    new Point(186, 237), new Point(193, 238), new Point(198, 239), new Point(200, 237),
                    new Point(202, 239), new Point(204, 238), new Point(206, 234), new Point(205, 230),
                    new Point(202, 222), new Point(197, 216), new Point(192, 207), new Point(186, 198),
                    new Point(179, 189), new Point(174, 183), new Point(170, 178), new Point(164, 171),
                    new Point(161, 168), new Point(154, 160), new Point(148, 155), new Point(143, 150),
                    new Point(138, 148), new Point(136, 148)));
            AddTemplate("triangle", point0);

            // x
            ArrayList<Point> point1 = new ArrayList(Arrays.asList(new Point(87, 142), new Point(89, 145), new Point(91, 148), new Point(93, 151),
                    new Point(96, 155), new Point(98, 157), new Point(100, 160), new Point(102, 162),
                    new Point(106, 167), new Point(108, 169), new Point(110, 171), new Point(115, 177),
                    new Point(119, 183), new Point(123, 189), new Point(127, 193), new Point(129, 196),
                    new Point(133, 200), new Point(137, 206), new Point(140, 209), new Point(143, 212),
                    new Point(146, 215), new Point(151, 220), new Point(153, 222), new Point(155, 223),
                    new Point(157, 225), new Point(158, 223), new Point(157, 218), new Point(155, 211),
                    new Point(154, 208), new Point(152, 200), new Point(150, 189), new Point(148, 179),
                    new Point(147, 170), new Point(147, 158), new Point(147, 148), new Point(147, 141),
                    new Point(147, 136), new Point(144, 135), new Point(142, 137), new Point(140, 139),
                    new Point(135, 145), new Point(131, 152), new Point(124, 163), new Point(116, 177),
                    new Point(108, 191), new Point(100, 206), new Point(94, 217), new Point(91, 222),
                    new Point(89, 225), new Point(87, 226), new Point(87, 224)));
            AddTemplate("x", point1);

            // rectangle
            ArrayList<Point> point2 = new ArrayList(Arrays.asList(new Point(78, 149), new Point(78, 153), new Point(78, 157), new Point(78, 160),
                    new Point(79, 162), new Point(79, 164), new Point(79, 167), new Point(79, 169),
                    new Point(79, 173), new Point(79, 178), new Point(79, 183), new Point(80, 189),
                    new Point(80, 193), new Point(80, 198), new Point(80, 202), new Point(81, 208),
                    new Point(81, 210), new Point(81, 216), new Point(82, 222), new Point(82, 224),
                    new Point(82, 227), new Point(83, 229), new Point(83, 231), new Point(85, 230),
                    new Point(88, 232), new Point(90, 233), new Point(92, 232), new Point(94, 233),
                    new Point(99, 232), new Point(102, 233), new Point(106, 233), new Point(109, 234),
                    new Point(117, 235), new Point(123, 236), new Point(126, 236), new Point(135, 237),
                    new Point(142, 238), new Point(145, 238), new Point(152, 238), new Point(154, 239),
                    new Point(165, 238), new Point(174, 237), new Point(179, 236), new Point(186, 235),
                    new Point(191, 235), new Point(195, 233), new Point(197, 233), new Point(200, 233),
                    new Point(201, 235), new Point(201, 233), new Point(199, 231), new Point(198, 226),
                    new Point(198, 220), new Point(196, 207), new Point(195, 195), new Point(195, 181),
                    new Point(195, 173), new Point(195, 163), new Point(194, 155), new Point(192, 145),
                    new Point(192, 143), new Point(192, 138), new Point(191, 135), new Point(191, 133),
                    new Point(191, 130), new Point(190, 128), new Point(188, 129), new Point(186, 129),
                    new Point(181, 132), new Point(173, 131), new Point(162, 131), new Point(151, 132),
                    new Point(149, 132), new Point(138, 132), new Point(136, 132), new Point(122, 131),
                    new Point(120, 131), new Point(109, 130), new Point(107, 130), new Point(90, 132),
                    new Point(81, 133), new Point(76, 133)));
            AddTemplate("rectangle", point2);

            // circle
            ArrayList<Point> point3 = new ArrayList(Arrays.asList(new Point(127, 141), new Point(124, 140), new Point(120, 139), new Point(118, 139),
                    new Point(116, 139), new Point(111, 140), new Point(109, 141), new Point(104, 144),
                    new Point(100, 147), new Point(96, 152), new Point(93, 157), new Point(90, 163),
                    new Point(87, 169), new Point(85, 175), new Point(83, 181), new Point(82, 190),
                    new Point(82, 195), new Point(83, 200), new Point(84, 205), new Point(88, 213),
                    new Point(91, 216), new Point(96, 219), new Point(103, 222), new Point(108, 224),
                    new Point(111, 224), new Point(120, 224), new Point(133, 223), new Point(142, 222),
                    new Point(152, 218), new Point(160, 214), new Point(167, 210), new Point(173, 204),
                    new Point(178, 198), new Point(179, 196), new Point(182, 188), new Point(182, 177),
                    new Point(178, 167), new Point(170, 150), new Point(163, 138), new Point(152, 130),
                    new Point(143, 129), new Point(140, 131), new Point(129, 136), new Point(126, 139)));
            AddTemplate("circle", point3);

            // check
            ArrayList<Point> point4 = new ArrayList(Arrays.asList(new Point(91, 185), new Point(93, 185), new Point(95, 185), new Point(97, 185), new Point(100, 188),
                    new Point(102, 189), new Point(104, 190), new Point(106, 193), new Point(108, 195), new Point(110, 198),
                    new Point(112, 201), new Point(114, 204), new Point(115, 207), new Point(117, 210), new Point(118, 212),
                    new Point(120, 214), new Point(121, 217), new Point(122, 219), new Point(123, 222), new Point(124, 224),
                    new Point(126, 226), new Point(127, 229), new Point(129, 231), new Point(130, 233), new Point(129, 231),
                    new Point(129, 228), new Point(129, 226), new Point(129, 224), new Point(129, 221), new Point(129, 218),
                    new Point(129, 212), new Point(129, 208), new Point(130, 198), new Point(132, 189), new Point(134, 182),
                    new Point(137, 173), new Point(143, 164), new Point(147, 157), new Point(151, 151), new Point(155, 144),
                    new Point(161, 137), new Point(165, 131), new Point(171, 122), new Point(174, 118), new Point(176, 114),
                    new Point(177, 112), new Point(177, 114), new Point(175, 116), new Point(173, 118)));
            AddTemplate("check", point4);

            // caret
            ArrayList<Point> point5 = new ArrayList(Arrays.asList(new Point(79, 245), new Point(79, 242), new Point(79, 239), new Point(80, 237), new Point(80, 234),
                    new Point(81, 232), new Point(82, 230), new Point(84, 224), new Point(86, 220), new Point(86, 218),
                    new Point(87, 216), new Point(88, 213), new Point(90, 207), new Point(91, 202), new Point(92, 200),
                    new Point(93, 194), new Point(94, 192), new Point(96, 189), new Point(97, 186), new Point(100, 179),
                    new Point(102, 173), new Point(105, 165), new Point(107, 160), new Point(109, 158), new Point(112, 151),
                    new Point(115, 144), new Point(117, 139), new Point(119, 136), new Point(119, 134), new Point(120, 132),
                    new Point(121, 129), new Point(122, 127), new Point(124, 125), new Point(126, 124), new Point(129, 125),
                    new Point(131, 127), new Point(132, 130), new Point(136, 139), new Point(141, 154), new Point(145, 166),
                    new Point(151, 182), new Point(156, 193), new Point(157, 196), new Point(161, 209), new Point(162, 211),
                    new Point(167, 223), new Point(169, 229), new Point(170, 231), new Point(173, 237), new Point(176, 242),
                    new Point(177, 244), new Point(179, 250), new Point(181, 255), new Point(182, 257)));
            AddTemplate("caret", point5);

            // question
            ArrayList<Point> point6 = new ArrayList(Arrays.asList(new Point(104, 145), new Point(103, 142), new Point(103, 140), new Point(103, 138), new Point(103, 135),
                    new Point(104, 133), new Point(105, 131), new Point(106, 128), new Point(107, 125), new Point(108, 123),
                    new Point(111, 121), new Point(113, 118), new Point(115, 116), new Point(117, 116), new Point(119, 116),
                    new Point(121, 115), new Point(124, 116), new Point(126, 115), new Point(128, 114), new Point(130, 115),
                    new Point(133, 116), new Point(135, 117), new Point(140, 120), new Point(142, 121), new Point(144, 123),
                    new Point(146, 125), new Point(149, 127), new Point(150, 129), new Point(152, 130), new Point(154, 132),
                    new Point(156, 134), new Point(158, 137), new Point(159, 139), new Point(160, 141), new Point(160, 143),
                    new Point(160, 146), new Point(160, 149), new Point(159, 153), new Point(158, 155), new Point(157, 157),
                    new Point(155, 159), new Point(153, 161), new Point(151, 163), new Point(146, 167), new Point(142, 170),
                    new Point(138, 172), new Point(134, 173), new Point(132, 175), new Point(127, 175), new Point(124, 175),
                    new Point(122, 176), new Point(120, 178), new Point(119, 180), new Point(119, 183), new Point(119, 185),
                    new Point(120, 190), new Point(121, 194), new Point(122, 200), new Point(123, 205), new Point(123, 211),
                    new Point(124, 215), new Point(124, 223), new Point(124, 225)));
            AddTemplate("question", point6);

            // arrow
            ArrayList<Point> point7 = new ArrayList(Arrays.asList(new Point(68, 222), new Point(70, 220),
                    new Point(73, 218), new Point(75, 217), new Point(77, 215),
                    new Point(80, 213), new Point(82, 212), new Point(84, 210),
                    new Point(87, 209), new Point(89, 208), new Point(92, 206),
                    new Point(95, 204), new Point(101, 201), new Point(106, 198),
                    new Point(112, 194), new Point(118, 191), new Point(124, 187),
                    new Point(127, 186), new Point(132, 183), new Point(138, 181),
                    new Point(141, 180), new Point(146, 178), new Point(154, 173),
                    new Point(159, 171), new Point(161, 170), new Point(166, 167),
                    new Point(168, 167), new Point(171, 166), new Point(174, 164),
                    new Point(177, 162), new Point(180, 160), new Point(182, 158),
                    new Point(183, 156), new Point(181, 154), new Point(178, 153),
                    new Point(171, 153), new Point(164, 153), new Point(160, 153),
                    new Point(150, 154), new Point(147, 155), new Point(141, 157),
                    new Point(137, 158), new Point(135, 158), new Point(137, 158),
                    new Point(140, 157), new Point(143, 156), new Point(151, 154),
                    new Point(160, 152), new Point(170, 149), new Point(179, 147),
                    new Point(185, 145), new Point(192, 144), new Point(196, 144),
                    new Point(198, 144), new Point(200, 144), new Point(201, 147),
                    new Point(199, 149), new Point(194, 157), new Point(191, 160),
                    new Point(186, 167), new Point(180, 176), new Point(177, 179),
                    new Point(171, 187), new Point(169, 189), new Point(165, 194),
                    new Point(164, 196)));
            AddTemplate("arrow", point7);

            // left square bracket
            ArrayList<Point> point8 = new ArrayList(Arrays.asList(new Point(140, 124), new Point(138, 123),
                    new Point(135, 122), new Point(133, 123), new Point(130, 123),
                    new Point(128, 124), new Point(125, 125), new Point(122, 124),
                    new Point(120, 124), new Point(118, 124), new Point(116, 125),
                    new Point(113, 125), new Point(111, 125), new Point(108, 124),
                    new Point(106, 125), new Point(104, 125), new Point(102, 124),
                    new Point(100, 123), new Point(98, 123), new Point(95, 124),
                    new Point(93, 123), new Point(90, 124), new Point(88, 124),
                    new Point(85, 125), new Point(83, 126), new Point(81, 127),
                    new Point(81, 129), new Point(82, 131), new Point(82, 134),
                    new Point(83, 138), new Point(84, 141), new Point(84, 144),
                    new Point(85, 148), new Point(85, 151), new Point(86, 156),
                    new Point(86, 160), new Point(86, 164), new Point(86, 168),
                    new Point(87, 171), new Point(87, 175), new Point(87, 179),
                    new Point(87, 182), new Point(87, 186), new Point(88, 188),
                    new Point(88, 195), new Point(88, 198), new Point(88, 201),
                    new Point(88, 207), new Point(89, 211), new Point(89, 213),
                    new Point(89, 217), new Point(89, 222), new Point(88, 225),
                    new Point(88, 229), new Point(88, 231), new Point(88, 233),
                    new Point(88, 235), new Point(89, 237), new Point(89, 240),
                    new Point(89, 242), new Point(91, 241), new Point(94, 241),
                    new Point(96, 240), new Point(98, 239), new Point(105, 240),
                    new Point(109, 240), new Point(113, 239), new Point(116, 240),
                    new Point(121, 239), new Point(130, 240), new Point(136, 237),
                    new Point(139, 237), new Point(144, 238), new Point(151, 237),
                    new Point(157, 236), new Point(159, 237)));
            AddTemplate("left square bracket", point8);

            // right square bracket.
            ArrayList<Point> point9 = new ArrayList(Arrays.asList(new Point(112, 138), new Point(112, 136),
                    new Point(115, 136), new Point(118, 137), new Point(120, 136),
                    new Point(123, 136), new Point(125, 136), new Point(128, 136),
                    new Point(131, 136), new Point(134, 135), new Point(137, 135),
                    new Point(140, 134), new Point(143, 133), new Point(145, 132),
                    new Point(147, 132), new Point(149, 132), new Point(152, 132),
                    new Point(153, 134), new Point(154, 137), new Point(155, 141),
                    new Point(156, 144), new Point(157, 152), new Point(158, 161),
                    new Point(160, 170), new Point(162, 182), new Point(164, 192),
                    new Point(166, 200), new Point(167, 209), new Point(168, 214),
                    new Point(168, 216), new Point(169, 221), new Point(169, 223),
                    new Point(169, 228), new Point(169, 231), new Point(166, 233),
                    new Point(164, 234), new Point(161, 235), new Point(155, 236),
                    new Point(147, 235), new Point(140, 233), new Point(131, 233),
                    new Point(124, 233), new Point(117, 235), new Point(114, 238),
                    new Point(112, 238)));
            AddTemplate("right square bracket", point9);

            // v
            ArrayList<Point> point10 = new ArrayList(Arrays.asList(new Point(89, 164), new Point(90, 162),
                    new Point(92, 162), new Point(94, 164), new Point(95, 166),
                    new Point(96, 169), new Point(97, 171), new Point(99, 175),
                    new Point(101, 178), new Point(103, 182), new Point(106, 189),
                    new Point(108, 194), new Point(111, 199), new Point(114, 204),
                    new Point(117, 209), new Point(119, 214), new Point(122, 218),
                    new Point(124, 222), new Point(126, 225), new Point(128, 228),
                    new Point(130, 229), new Point(133, 233), new Point(134, 236),
                    new Point(136, 239), new Point(138, 240), new Point(139, 242),
                    new Point(140, 244), new Point(142, 242), new Point(142, 240),
                    new Point(142, 237), new Point(143, 235), new Point(143, 233),
                    new Point(145, 229), new Point(146, 226), new Point(148, 217),
                    new Point(149, 208), new Point(149, 205), new Point(151, 196),
                    new Point(151, 193), new Point(153, 182), new Point(155, 172),
                    new Point(157, 165), new Point(159, 160), new Point(162, 155),
                    new Point(164, 150), new Point(165, 148), new Point(166, 146)));
            AddTemplate("v", point10);

            // delete
            ArrayList<Point> point11 = new ArrayList(Arrays.asList(new Point(123, 129), new Point(123, 131),
                    new Point(124, 133), new Point(125, 136), new Point(127, 140),
                    new Point(129, 142), new Point(133, 148), new Point(137, 154),
                    new Point(143, 158), new Point(145, 161), new Point(148, 164),
                    new Point(153, 170), new Point(158, 176), new Point(160, 178),
                    new Point(164, 183), new Point(168, 188), new Point(171, 191),
                    new Point(175, 196), new Point(178, 200), new Point(180, 202),
                    new Point(181, 205), new Point(184, 208), new Point(186, 210),
                    new Point(187, 213), new Point(188, 215), new Point(186, 212),
                    new Point(183, 211), new Point(177, 208), new Point(169, 206),
                    new Point(162, 205), new Point(154, 207), new Point(145, 209),
                    new Point(137, 210), new Point(129, 214), new Point(122, 217),
                    new Point(118, 218), new Point(111, 221), new Point(109, 222),
                    new Point(110, 219), new Point(112, 217), new Point(118, 209),
                    new Point(120, 207), new Point(128, 196), new Point(135, 187),
                    new Point(138, 183), new Point(148, 167), new Point(157, 153),
                    new Point(163, 145), new Point(165, 142), new Point(172, 133),
                    new Point(177, 127), new Point(179, 127), new Point(180, 125)));
            AddTemplate("delete", point11);

            // left curly brace
            ArrayList<Point> point12 = new ArrayList(Arrays.asList(new Point(150, 116), new Point(147, 117),
                    new Point(145, 116), new Point(142, 116), new Point(139, 117),
                    new Point(136, 117), new Point(133, 118), new Point(129, 121),
                    new Point(126, 122), new Point(123, 123), new Point(120, 125),
                    new Point(118, 127), new Point(115, 128), new Point(113, 129),
                    new Point(112, 131), new Point(113, 134), new Point(115, 134),
                    new Point(117, 135), new Point(120, 135), new Point(123, 137),
                    new Point(126, 138), new Point(129, 140), new Point(135, 143),
                    new Point(137, 144), new Point(139, 147), new Point(141, 149),
                    new Point(140, 152), new Point(139, 155), new Point(134, 159),
                    new Point(131, 161), new Point(124, 166), new Point(121, 166),
                    new Point(117, 166), new Point(114, 167), new Point(112, 166),
                    new Point(114, 164), new Point(116, 163), new Point(118, 163),
                    new Point(120, 162), new Point(122, 163), new Point(125, 164),
                    new Point(127, 165), new Point(129, 166), new Point(130, 168),
                    new Point(129, 171), new Point(127, 175), new Point(125, 179),
                    new Point(123, 184), new Point(121, 190), new Point(120, 194),
                    new Point(119, 199), new Point(120, 202), new Point(123, 207),
                    new Point(127, 211), new Point(133, 215), new Point(142, 219),
                    new Point(148, 220), new Point(151, 221)));
            AddTemplate("left curly brace", point12);

            // right curly brace
            ArrayList<Point> point13 = new ArrayList(Arrays.asList(new Point(117, 132), new Point(115, 132),
                    new Point(115, 129), new Point(117, 129), new Point(119, 128),
                    new Point(122, 127), new Point(125, 127), new Point(127, 127),
                    new Point(130, 127), new Point(133, 129), new Point(136, 129),
                    new Point(138, 130), new Point(140, 131), new Point(143, 134),
                    new Point(144, 136), new Point(145, 139), new Point(145, 142),
                    new Point(145, 145), new Point(145, 147), new Point(145, 149),
                    new Point(144, 152), new Point(142, 157), new Point(141, 160),
                    new Point(139, 163), new Point(137, 166), new Point(135, 167),
                    new Point(133, 169), new Point(131, 172), new Point(128, 173),
                    new Point(126, 176), new Point(125, 178), new Point(125, 180),
                    new Point(125, 182), new Point(126, 184), new Point(128, 187),
                    new Point(130, 187), new Point(132, 188), new Point(135, 189),
                    new Point(140, 189), new Point(145, 189), new Point(150, 187),
                    new Point(155, 186), new Point(157, 185), new Point(159, 184),
                    new Point(156, 185), new Point(154, 185), new Point(149, 185),
                    new Point(145, 187), new Point(141, 188), new Point(136, 191),
                    new Point(134, 191), new Point(131, 192), new Point(129, 193),
                    new Point(129, 195), new Point(129, 197), new Point(131, 200),
                    new Point(133, 202), new Point(136, 206), new Point(139, 211),
                    new Point(142, 215), new Point(145, 220), new Point(147, 225),
                    new Point(148, 231), new Point(147, 239), new Point(144, 244),
                    new Point(139, 248), new Point(134, 250), new Point(126, 253),
                    new Point(119, 253), new Point(115, 253)));
            AddTemplate("right curly brace", point13);

            // star
            ArrayList<Point> point14 = new ArrayList(Arrays.asList(new Point(75, 250), new Point(75, 247),
                    new Point(77, 244), new Point(78, 242), new Point(79, 239),
                    new Point(80, 237), new Point(82, 234), new Point(82, 232),
                    new Point(84, 229), new Point(85, 225), new Point(87, 222),
                    new Point(88, 219), new Point(89, 216), new Point(91, 212),
                    new Point(92, 208), new Point(94, 204), new Point(95, 201),
                    new Point(96, 196), new Point(97, 194), new Point(98, 191),
                    new Point(100, 185), new Point(102, 178), new Point(104, 173),
                    new Point(104, 171), new Point(105, 164), new Point(106, 158),
                    new Point(107, 156), new Point(107, 152), new Point(108, 145),
                    new Point(109, 141), new Point(110, 139), new Point(112, 133),
                    new Point(113, 131), new Point(116, 127), new Point(117, 125),
                    new Point(119, 122), new Point(121, 121), new Point(123, 120),
                    new Point(125, 122), new Point(125, 125), new Point(127, 130),
                    new Point(128, 133), new Point(131, 143), new Point(136, 153),
                    new Point(140, 163), new Point(144, 172), new Point(145, 175),
                    new Point(151, 189), new Point(156, 201), new Point(161, 213),
                    new Point(166, 225), new Point(169, 233), new Point(171, 236),
                    new Point(174, 243), new Point(177, 247), new Point(178, 249),
                    new Point(179, 251), new Point(180, 253), new Point(180, 255),
                    new Point(179, 257), new Point(177, 257), new Point(174, 255),
                    new Point(169, 250), new Point(164, 247), new Point(160, 245),
                    new Point(149, 238), new Point(138, 230), new Point(127, 221),
                    new Point(124, 220), new Point(112, 212), new Point(110, 210),
                    new Point(96, 201), new Point(84, 195), new Point(74, 190),
                    new Point(64, 182), new Point(55, 175), new Point(51, 172),
                    new Point(49, 170), new Point(51, 169), new Point(56, 169),
                    new Point(66, 169), new Point(78, 168), new Point(92, 166),
                    new Point(107, 164), new Point(123, 161), new Point(140, 162),
                    new Point(156, 162), new Point(171, 160), new Point(173, 160),
                    new Point(186, 160), new Point(195, 160), new Point(198, 161),
                    new Point(203, 163), new Point(208, 163), new Point(206, 164),
                    new Point(200, 167), new Point(187, 172), new Point(174, 179),
                    new Point(172, 181), new Point(153, 192), new Point(137, 201),
                    new Point(123, 211), new Point(112, 220), new Point(99, 229),
                    new Point(90, 237), new Point(80, 244), new Point(73, 250),
                    new Point(69, 254), new Point(69, 252)));
            AddTemplate("star", point14);

            // pig tail
            ArrayList<Point> point15 = new ArrayList(Arrays.asList(new Point(81, 219), new Point(84, 218),
                    new Point(86, 220), new Point(88, 220), new Point(90, 220),
                    new Point(92, 219), new Point(95, 220), new Point(97, 219),
                    new Point(99, 220), new Point(102, 218), new Point(105, 217),
                    new Point(107, 216), new Point(110, 216), new Point(113, 214),
                    new Point(116, 212), new Point(118, 210), new Point(121, 208),
                    new Point(124, 205), new Point(126, 202), new Point(129, 199),
                    new Point(132, 196), new Point(136, 191), new Point(139, 187),
                    new Point(142, 182), new Point(144, 179), new Point(146, 174),
                    new Point(148, 170), new Point(149, 168), new Point(151, 162),
                    new Point(152, 160), new Point(152, 157), new Point(152, 155),
                    new Point(152, 151), new Point(152, 149), new Point(152, 146),
                    new Point(149, 142), new Point(148, 139), new Point(145, 137),
                    new Point(141, 135), new Point(139, 135), new Point(134, 136),
                    new Point(130, 140), new Point(128, 142), new Point(126, 145),
                    new Point(122, 150), new Point(119, 158), new Point(117, 163),
                    new Point(115, 170), new Point(114, 175), new Point(117, 184),
                    new Point(120, 190), new Point(125, 199), new Point(129, 203),
                    new Point(133, 208), new Point(138, 213), new Point(145, 215),
                    new Point(155, 218), new Point(164, 219), new Point(166, 219),
                    new Point(177, 219), new Point(182, 218), new Point(192, 216),
                    new Point(196, 213), new Point(199, 212), new Point(201, 211)));
            AddTemplate("pigtail", point15);
        }

        int AddTemplate(String name, ArrayList<Point> points) {
            templates.add(new Template(name, points));
            int num = 0;
            for (int i = 0; i < templates.size(); i++) {
                if (templates.get(i).Name == name) {
                    num++;
                }
            }
            return num;
        }

        /*void DeleteUserTemplates() {
         templates = (Template[]) subset(templates, 0, NumTemplates);
         }*/
        public Result recognize(ArrayList<Point> points) {
            points = resample(points, numPoints);
            points = rotateToZero(points);
            points = scaleToSquare(points, squareSize);
            points = translateToOrigin(points);

            double best = Double.POSITIVE_INFINITY;
            double sndBest = Double.POSITIVE_INFINITY;
            int t = -1;

            for (int i = 0; i < templates.size(); i++) {
                double d = DistanceAtBestAngle(points, templates.get(i),
                        -angleRange, angleRange, anglePrecision);
                if (d < best) {
                    sndBest = best;
                    best = d;
                    t = i;
                } else if (d < sndBest) {
                    sndBest = d;
                }
            }

            double score = 1.0 - (best / halfDiagonal);
            double otherScore = 1.0 - (sndBest / halfDiagonal);
            double ratio = otherScore / score;

            // The threshold of 0.7 is arbitrary, and not part of the original code.
            if (t > -1 && score > 0.7) {
                return new Result(templates.get(t).Name, score, ratio);
            } else {
                return new Result("- none - ", 0.0, 1.0);
            }
        }
    }

    private ArrayList<Point> rotateToZero(ArrayList<Point> points) {
        Point c = centroid(points);
        Double theta = Math.atan2(c.y - points.get(0).y,
                c.x - points.get(0).x);
        return rotateBy(points, -theta);
    }

    private ArrayList<Point> rotateBy(ArrayList<Point> points, double theta) {
        Point c = centroid(points);
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);

        ArrayList<Point> newpoints = new ArrayList<Point>();
        for (int i = 0; i < points.size(); i++) {
            int qx = (int) ((points.get(i).x - c.x) * cos - (points.get(i).y - c.y) * sin + c.x);
            int qy = (int) ((points.get(i).x - c.x) * sin + (points.get(i).y - c.y) * cos + c.y);
            newpoints.add(new Point(qx, qy));
        }
        return newpoints;
    }

    private Point centroid(ArrayList<Point> points) {
        Point centroid = new Point(0, 0);
        for (int i = 1; i < points.size(); i++) {
            centroid.x += points.get(i).getX();
            centroid.y += points.get(i).getY();
        }
        centroid.x /= points.size();
        centroid.y /= points.size();

        return centroid;
    }

    private ArrayList<Point> scaleToSquare(ArrayList<Point> points, double sz) {
        Rectangle b = boundingBox(points);

        ArrayList<Point> newpoints = new ArrayList<Point>();
        for (int i = 0; i < points.size(); i++) {
            int qx = (int) (points.get(i).x * (sz / b.getWidth()));
            int qy = (int) (points.get(i).y * (sz / b.getHeight()));
            newpoints.add(new Point(qx, qy));
        }
        return newpoints;
    }

    private double DistanceAtBestAngle(ArrayList<Point> points, Template T,
        double a, double b, double threshold) {
        double x1 = Phi * a + (1.0 - Phi) * b;
        double f1 = DistanceAtAngle(points, T, x1);
        double x2 = (1.0 - Phi) * a + Phi * b;
        double f2 = DistanceAtAngle(points, T, x2);
        while (Math.abs(b - a) > threshold) {
            if (f1 < f2) {
                b = x2;
                x2 = x1;
                f2 = f1;
                x1 = Phi * a + (1.0 - Phi) * b;
                f1 = DistanceAtAngle(points, T, x1);
            } else {
                a = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1.0 - Phi) * a + Phi * b;
                f2 = DistanceAtAngle(points, T, x2);
            }
        }
        return Math.min(f1, f2);
    }

    private double DistanceAtAngle(ArrayList<Point> points, Template T, double theta) {
        ArrayList<Point> newpoints = rotateBy(points, theta);
        return pathDistance(newpoints, T.Points);
    }

    private double pathDistance(ArrayList<Point> pts1, ArrayList<Point> pts2) {
        if (pts1.size() != pts2.size()) {
            //System.out.println("Lengths differ. " + pts1.size() + " != " + pts2.size());
            return Double.POSITIVE_INFINITY;
        }

        double d = 0.0;
        for (int i = 0; i < pts1.size(); i++) {
            d += pts1.get(i).distance(pts2.get(i));
        }
        return d / pts1.size();
    }

    private Rectangle boundingBox(ArrayList<Point> points) {
        int minX = (int) Double.POSITIVE_INFINITY;
        int maxX = (int) Double.NEGATIVE_INFINITY;
        int minY = (int) Double.POSITIVE_INFINITY;
        int maxY = (int) Double.NEGATIVE_INFINITY;

        for (int i = 1; i < points.size(); i++) {
            minX = Math.min(points.get(i).x, minX);
            maxX = Math.max(points.get(i).x, maxX);
            minY = Math.min(points.get(i).y, minY);
            maxY = Math.max(points.get(i).y, maxY);
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    private ArrayList<Point> translateToOrigin(ArrayList<Point> points) {
        Point c = centroid(points);

        ArrayList<Point> newpoints = new ArrayList<Point>();
        for (int i = -0; i < points.size(); i++) {
            int qx = (int) (points.get(i).x - c.x);
            int qy = (int) (points.get(i).y - c.y);
            newpoints.add(new Point(qx, qy));
        }

        return newpoints;
    }
}
