package com.nutri_sci.ui;

import com.nutri_sci.controller.VisualizationController;
import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.CanadaFoodGuideService;
import com.nutri_sci.service.ChartRenderer;
import com.nutri_sci.service.NutrientCalculator;

import javax.swing.*;
import java.util.Date;

/**
 * Main visualization hub that coordinates different visualization panels.
 * Refactored to use composition with extracted panel classes.
 */
public class VisualizationHubUI extends JFrame {

    private final UserProfile userProfile;
    private final VisualizationController controller;
    private final ChartRenderer chartRenderer;
    private final CanadaFoodGuideService cfgService;

    // Extracted panels
    private DailyIntakePanel dailyIntakePanel;
    private SwapEffectPanel swapEffectPanel;
    private CfgAlignmentPanel cfgAlignmentPanel;

    public VisualizationHubUI(UserProfile userProfile) {
        this.userProfile = userProfile;
        DBManager dbManager = DBManager.getInstance();
        NutrientCalculator nutrientCalculator = new NutrientCalculator(dbManager);
        this.controller = new VisualizationController(userProfile, dbManager, nutrientCalculator);
        this.chartRenderer = new ChartRenderer();
        this.cfgService = new CanadaFoodGuideService();

        setTitle("NutriSci - Visualization Hub");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializePanels();
        layoutComponents();
        setSmartDefaultDate();
    }

    private void initializePanels() {
        dailyIntakePanel = new DailyIntakePanel(controller, chartRenderer);
        swapEffectPanel = new SwapEffectPanel(controller, chartRenderer);
        cfgAlignmentPanel = new CfgAlignmentPanel(controller, chartRenderer, cfgService);
    }

    private void layoutComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Daily Intake Analysis", dailyIntakePanel);
        tabbedPane.addTab("Swap Effect Analysis", swapEffectPanel);
        tabbedPane.addTab("CFG Alignment", cfgAlignmentPanel);
        add(tabbedPane);
    }

    private void setSmartDefaultDate() {
        Date mostRecentMealDate = DBManager.getInstance().getMostRecentMealDate(userProfile.getId());
        if (mostRecentMealDate != null) {
            dailyIntakePanel.setDefaultDate(mostRecentMealDate);
            swapEffectPanel.setDefaultDate(mostRecentMealDate);
            cfgAlignmentPanel.setDefaultDate(mostRecentMealDate);
        }
    }
}