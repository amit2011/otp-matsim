package examples;

/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

import java.io.File;
import javax.inject.Inject;
import javax.inject.Provider;
import com.google.inject.Singleton;
import core.OTPRoutingModule;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.minibus.performance.raptor.Raptor;
import org.matsim.contrib.minibus.performance.raptor.RaptorDisutility;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.pt.router.TransitRouter;
import org.matsim.pt.router.TransitRouterConfig;
import org.matsim.pt.router.TransitRouterImplFactory;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.InputStreamGraphSource;
import org.opentripplanner.routing.services.GraphService;

/**
 * Created by amit on 24.11.17.
 */

public class RunExample {

    public static void main(String[] args) {

        String configFile = args[0];
        String transitRouterType = args[1];
        Config config = ConfigUtils.loadConfig(configFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);

        Controler controler = new Controler(scenario);

        /**
         * The idea is to have a configurable transit router.
         *
         */
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // Dijkstra
                if (transitRouterType.equals("standard")) {
                    bind(TransitRouter.class).toProvider(TransitRouterImplFactory.class);
                } else if(transitRouterType.equals("raptor")) {
                    bind(TransitRouter.class).toProvider(RaptorTransitRouterProvider.class);
                } else if (transitRouterType.equals("otp")) {
                    bind(TransitRouter.class).toProvider(OTPRouterProvider.class);
                } else if (transitRouterType.equals("r5")) {
                    bind(TransitRouter.class).toProvider(R5TransitRouterProvider.class);
                } else {
                    throw new RuntimeException("not implemented yet.");
                }
            }
        });

        controler.run();
    }

    @Singleton
    private static class RaptorTransitRouterProvider implements Provider<TransitRouter> {

        private final RaptorDisutility raptorDisutility;
        private final TransitRouterConfig transitRouterConfig;
        private final TransitSchedule schedule;

        @Inject
        RaptorTransitRouterProvider(final TransitSchedule schedule, final Config config) {
            //TODO configure these magic numbers. They are in PConfigGroup.
            double costPerMeterTraveled = 0.;
            double costPerBoarding = 0.;


            this.schedule = schedule;
            this.transitRouterConfig = new TransitRouterConfig(
                    config.planCalcScore(),
                    config.plansCalcRoute(),
                    config.transitRouter(),
                    config.vspExperimental());
            this.raptorDisutility = new RaptorDisutility(this.transitRouterConfig,
                    costPerBoarding, costPerMeterTraveled);
        }

        @Override
        public TransitRouter get() {
            return new Raptor(this.transitRouterConfig, this.schedule, this.raptorDisutility);
        }
    }

    private static class OTPRouterProvider implements Provider<TransitRouter>{
        private CoordinateTransformation ct;
        private String day;
        private String timeZone;
        private TransitSchedule transitSchedule;
        private Network matsimNetwork;
        private boolean chooseRandomlyAnOtpParameterProfile;
        private int numOfAlternativeItinerariesToChooseFromRandomly;
        private final GraphService graphservice;
        private final boolean useCreatePseudoNetworkInsteadOfOtpPtNetwork;

        public OTPRouterProvider(TransitSchedule transitSchedule, Network matsimNetwork,
                                    CoordinateTransformation ct, String day, String timeZone, String graphFile,
                                    boolean chooseRandomlyAnOtpParameterProfile,
                                    int numOfAlternativeItinerariesToChooseFromRandomly,
                                    boolean useCreatePseudoNetworkInsteadOfOtpPtNetwork) {
            graphservice = createGraphService(graphFile);
            this.transitSchedule = transitSchedule;
            this.matsimNetwork = matsimNetwork;
            this.ct = ct;
            this.day = day;
            this.timeZone = timeZone;
            this.chooseRandomlyAnOtpParameterProfile = chooseRandomlyAnOtpParameterProfile;
            this.numOfAlternativeItinerariesToChooseFromRandomly = numOfAlternativeItinerariesToChooseFromRandomly;
            this.useCreatePseudoNetworkInsteadOfOtpPtNetwork = useCreatePseudoNetworkInsteadOfOtpPtNetwork;
        }

        public static GraphService createGraphService(String graphFile) {
            GraphService graphService = new GraphService();
            graphService.registerGraph("", InputStreamGraphSource.newFileGraphSource("", new File(graphFile), Graph.LoadLevel.FULL));
            return graphService;
        }

        @Override
        public TransitRouter get() {
            new OTPRoutingModule(graphservice,
                    transitSchedule,
                    matsimNetwork,
                    day,
                    timeZone,
                    ct,
                    chooseRandomlyAnOtpParameterProfile,
                    numOfAlternativeItinerariesToChooseFromRandomly,
                    useCreatePseudoNetworkInsteadOfOtpPtNetwork);
            return null;
        }
    }

    private static class R5TransitRouterProvider implements Provider<TransitRouter> {
        @Override
        public TransitRouter get() {
            throw new RuntimeException("not implemented yet.");
        }
    }
}
