package com.example.ivangarrera.example.Controller;

import android.util.Log;

import com.fuzzylite.Engine;
import com.fuzzylite.activation.General;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.s.AlgebraicSum;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.AlgebraicProduct;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Trapezoid;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;

import java.util.concurrent.Semaphore;

public class FuzzyRiskAnalysis {
    private static Engine engine = null;
    private static Semaphore mutex = new Semaphore(1, true);

    public static void Build() {
        try {
            mutex.acquire(1);
            if (engine == null) {
                engine = new Engine();
                engine.setName("RiskAnalysis");
                engine.setDescription("");

                InputVariable humidity = new InputVariable();
                humidity.setName("humidity");
                humidity.setDescription("");
                humidity.setEnabled(true);
                humidity.setRange(0.000, 100.000);
                humidity.setLockValueInRange(false);
                humidity.addTerm(new Trapezoid("veryLow", 0.000, 0.000, 20.000, 30.000));
                humidity.addTerm(new Trapezoid("low", 20.000, 30.000, 40.000, 50.000));
                humidity.addTerm(new Trapezoid("mid", 40.000, 50.000, 60.000, 70.000));
                humidity.addTerm(new Trapezoid("high", 60.000, 70.000, 80.000, 90.000));
                humidity.addTerm(new Trapezoid("veryHigh", 80.000, 90.000, 100.000, 100.000));
                engine.addInputVariable(humidity);

                InputVariable rain = new InputVariable();
                rain.setName("rain");
                rain.setDescription("");
                rain.setEnabled(true);
                rain.setRange(0.000, 1023.000);
                rain.setLockValueInRange(false);
                rain.addTerm(new Trapezoid("yes", 0.000, 0.000, 462.000, 562.000));
                rain.addTerm(new Trapezoid("no", 462.000, 562.000, 1023.000, 1023.000));
                engine.addInputVariable(rain);

                InputVariable light = new InputVariable();
                light.setName("light");
                light.setDescription("");
                light.setEnabled(true);
                light.setRange(0.000, 1023.000);
                light.setLockValueInRange(false);
                light.addTerm(new Trapezoid("night", 0.000, 0.000, 100.000, 200.000));
                light.addTerm(new Trapezoid("dark", 100.000, 200.000, 300.000, 400.000));
                light.addTerm(new Trapezoid("mid", 300.000, 400.000, 500.000, 600.000));
                light.addTerm(new Trapezoid("day", 500.000, 600.000, 800.000, 900.000));
                light.addTerm(new Trapezoid("bright", 800.000, 900.000, 1023.000, 1023.000));
                engine.addInputVariable(light);

                OutputVariable risk = new OutputVariable();
                risk.setName("risk");
                risk.setDescription("");
                risk.setEnabled(true);
                risk.setRange(0.000, 1.000);
                risk.setLockValueInRange(false);
                risk.setAggregation(new Maximum());
                risk.setDefuzzifier(new Centroid(100));
                risk.setDefaultValue(Double.NaN);
                risk.setLockPreviousValue(false);
                risk.addTerm(new Trapezoid("low", 0.000, 0.000, 0.300, 0.400));
                risk.addTerm(new Trapezoid("mid", 0.300, 0.400, 0.700, 0.800));
                risk.addTerm(new Trapezoid("high", 0.700, 0.800, 1.000, 1.000));
                engine.addOutputVariable(risk);

                RuleBlock ruleBlock = new RuleBlock();
                ruleBlock.setName("ruleBlock");
                ruleBlock.setDescription("");
                ruleBlock.setEnabled(true);
                ruleBlock.setConjunction(new AlgebraicProduct());
                ruleBlock.setDisjunction(new AlgebraicSum());
                ruleBlock.setImplication(new AlgebraicProduct());
                ruleBlock.setActivation(new General());
                ruleBlock.addRule(Rule.parse("if rain is yes and (light is bright or light is day) and (humidity is veryLow or humidity is low or humidity is mid) then risk is low", engine));
                ruleBlock.addRule(Rule.parse("if rain is yes and (light is bright or light is day) and (humidity is high or humidity is veryHigh) then risk is mid", engine));
                ruleBlock.addRule(Rule.parse("if rain is yes and light is mid and (humidity is veryLow or humidity is low or humidity is mid) then risk is mid", engine));
                ruleBlock.addRule(Rule.parse("if (humidity is high or humidity is veryHigh) and rain is yes then risk is high", engine));
                ruleBlock.addRule(Rule.parse("if rain is yes and (light is dark or light is night) then risk is high", engine));
                ruleBlock.addRule(Rule.parse("if rain is yes and light is mid and (humidity is high or humidity is veryHigh) then risk is high", engine));

                ruleBlock.addRule(Rule.parse("if rain is no and (light is bright or light is day) then risk is low", engine));
                ruleBlock.addRule(Rule.parse("if rain is no and light is mid and (humidity is veryLow or humidity is low or humidity is mid) then risk is low", engine));
                ruleBlock.addRule(Rule.parse("if rain is no and light is mid and (humidity is high or humidity is veryHigh) then risk is mid", engine));
                ruleBlock.addRule(Rule.parse("if rain is no and light is dark and (humidity is veryLow or humidity is low or humidity is mid) then risk is mid", engine));
                ruleBlock.addRule(Rule.parse("if rain is no and light is dark and (humidity is high or humidity is veryHigh) then risk is high", engine));
                ruleBlock.addRule(Rule.parse("if rain is no and light is night and (humidity is veryLow or humidity is low) then risk is mid", engine));
                ruleBlock.addRule(Rule.parse("if rain is no and light is night and (humidity is mid or humidity is high or humidity is veryHigh) then risk is high", engine));
                engine.addRuleBlock(ruleBlock);

                StringBuilder status = new StringBuilder();
                if (!engine.isReady(status)) {
                    throw new RuntimeException("The engine is not ready: " + status);
                }
            }
        } catch (InterruptedException ex) {
            Log.e("GVIDI", "Mutex acquire error in FuzzyRiskAnalysis");
        } finally {
            mutex.release(1);
        }
    }

    public static String CalculateRisk(double humidity, double rain, double light) {
        if (engine != null) {
            String output = null;
            try {
                mutex.acquire(1);
                InputVariable humidity_fuzz = engine.getInputVariable("humidity");
                InputVariable rain_fuzz = engine.getInputVariable("rain");
                InputVariable light_fuzz = engine.getInputVariable("light");
                OutputVariable risk = engine.getOutputVariable("risk");

                humidity_fuzz.setValue(humidity);
                rain_fuzz.setValue(rain);
                light_fuzz.setValue(light);
                engine.process();

                output = risk.fuzzyOutputValue();
            } catch (InterruptedException ex) {
                Log.e("GVIDI", "Mutex acquire error in FuzzyRiskAnalysis");
            } finally {
                mutex.release(1);
                return output;
            }
        } else {
            throw new RuntimeException("The engine is null. You must call FuzzyRiskAnalysis.Build() before call CalculateRisk");
        }
    }
}
