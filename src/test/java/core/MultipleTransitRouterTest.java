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

package org.matsim;

import java.util.Arrays;
import java.util.Collection;
import core.OTPRoutingModule;
import org.apache.log4j.Logger;
import org.junit.runners.Parameterized;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.minibus.performance.raptor.Raptor;
import org.matsim.core.utils.geometry.transformations.IdentityTransformation;
import org.matsim.pt.router.TransitRouter;
import org.matsim.pt.router.TransitRouterConfig;
import org.matsim.pt.router.TransitRouterImpl;
import org.matsim.pt.router.TransitRouterImplTest;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.opentripplanner.routing.services.GraphService;

/**
 * Created by amit on 17.10.17.
 */

public class MultipleTransitRouterTest extends TransitRouterImplTest {

    private static final Logger log = Logger.getLogger(MultipleTransitRouterTest.class) ;
    private final String routerType;

    @Parameterized.Parameters(name = "{index}: TransitRouter == {0}")
    public static Collection<Object> createRouterTypes() {
        Object[] router = new Object [] {
                "standard",
                "raptor",
//                "connectionScan",
//                "otp",
//                "r5"
        };
        return Arrays.asList(router);
    }

    public MultipleTransitRouterTest(String routerType) {
        super(routerType);
        log.warn( "using router=" + routerType ) ;
        this.routerType = routerType;
    }

    protected TransitRouter createTransitRouter(TransitSchedule schedule, TransitRouterConfig trConfig, String routerType) {
        TransitRouter router = null ;
        switch( routerType ) {
            case "standard":
                router = new TransitRouterImpl(trConfig, schedule);
                break;
            case "raptor":
//                double costPerMeterTraveled = 0.;
//                double costPerBoarding = 0.;
//                RaptorDisutility raptorDisutility = new RaptorDisutility(trConfig, costPerBoarding, costPerMeterTraveled);
//                TransitRouterQuadTree transitRouterQuadTree = new TransitRouterQuadTree(raptorDisutility);
//                transitRouterQuadTree.initializeFromSchedule(schedule, trConfig.getBeelineWalkConnectionDistance());
//                router = new Raptor(transitRouterQuadTree, raptorDisutility, trConfig) ;
                router = new Raptor( trConfig, schedule) ;
                break;
            case "connectionScan":
                throw new RuntimeException("not implemented yet.");
            case "otp":
//                GraphService graphService = new GraphService();
//                Network network = null;
//                router = new OTPRoutingModule(graphService,schedule, network, "2017-11-06", "GMT+1",
//                        new IdentityTransformation(), false,1, false);
                break;
            default:
                break;
        }
        return router;
    }
}
