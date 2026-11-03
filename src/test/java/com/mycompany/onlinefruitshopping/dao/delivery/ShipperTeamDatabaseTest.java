package com.mycompany.onlinefruitshopping.dao.delivery;

import com.mycompany.onlinefruitshopping.support.ModuleDatabase;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShipperTeamDatabaseTest {
    ModuleDatabase db;
    ShipperTeamDAO teams;
    @BeforeEach void setup() throws Exception {
        db=new ModuleDatabase();teams=new ShipperTeamDAO(db::open);
    }
    void unassignedShipper() throws Exception {
        db.exec("DELETE FROM shop_delivery_settings WHERE shipper_id=21",
                "DELETE FROM shop_delivery_staff WHERE shipper_id=21");
    }
    int pendingRequest() throws Exception {
        return db.count("SELECT request_id FROM shop_delivery_requests WHERE shipper_id=21 AND status='Pending'");
    }

    @Test void applyReviewLeaveAndRejoinReuseCompositeMembershipKey() throws Exception {
        unassignedShipper();teams.applyToShop(21,1);
        var request=teams.getRequests(10).get(0);
        assertEquals("Shop A",request.getShopName());
        assertEquals("shipper2@test.invalid",request.getShipperEmail());
        teams.reviewRequest(10,pendingRequest(),false,"No vacancy");
        var rejected=teams.getMyRequests(21).get(0);
        assertEquals("No vacancy",rejected.getReviewNote());
        assertNotNull(rejected.getReviewedAt());
        teams.applyToShop(21,1);teams.reviewRequest(10,pendingRequest(),true,"");
        assertEquals("Active",teams.getMyMembership(21).getStatus());
        teams.requestLeave(21);
        assertNull(teams.getMyMembership(21));
        teams.applyToShop(21,1);teams.reviewRequest(10,pendingRequest(),true,"");
        assertEquals(1,db.count("SELECT COUNT(*) FROM shop_delivery_staff WHERE shop_id=1 AND shipper_id=21"));
        assertTrue(teams.getMyMembership(21).isAvailable());
        assertFalse(teams.getMyMembership(21).isLeaveRequested());
    }

    @Test void availabilityAndOwnerAssignmentFlagControlAssignment() throws Exception {
        AssignmentDAO assignments=new AssignmentDAO(db::open);
        teams.setAvailability(20,false);
        assertThrows(IllegalArgumentException.class,()->assignments.assign(10,101,20,"Pickup"));
        teams.setAvailability(20,true);
        teams.setAssignmentEnabled(10,20,false);
        assertThrows(IllegalArgumentException.class,()->assignments.assign(10,101,20,"Pickup"));
        teams.setAssignmentEnabled(10,20,true);
        assignments.assign(10,101,20,"Pickup");
        assertEquals(2,teams.getMyMembership(20).getOpenDeliveryCount());
    }

    @Test void terminationWaitsForExistingDeliveryButBlocksNewWork() throws Exception {
        teams.terminateMember(10,20);
        assertTrue(teams.getMyMembership(20).isLeaveRequested());
        assertThrows(IllegalArgumentException.class,()->new AssignmentDAO(db::open).assign(10,101,20,"Pickup"));
        new DeliveryDAO(db::open).updateStatus(1,20,"delivered","",true);
        assertNull(teams.getMyMembership(20));
        assertEquals("Inactive",db.scalar("SELECT status FROM shop_delivery_staff WHERE shipper_id=20"));
    }

    @Test void voluntaryLeaveAlsoWaitsForDeliveryCompletion() throws Exception {
        teams.requestLeave(20);
        assertTrue(teams.getMyMembership(20).isLeaveRequested());
        new DeliveryDAO(db::open).updateStatus(1,20,"failed","Customer unavailable",false);
        assertNull(teams.getMyMembership(20));
    }

    @Test void anotherOwnerCannotManageMembersOrApproveRequest() throws Exception {
        assertThrows(IllegalArgumentException.class,()->teams.setAssignmentEnabled(11,20,false));
        assertThrows(IllegalArgumentException.class,()->teams.terminateMember(11,20));
        assertTrue(teams.getMyMembership(20).isAssignmentEnabled());
        unassignedShipper();teams.applyToShop(21,1);
        int request=pendingRequest();
        assertThrows(IllegalArgumentException.class,()->teams.reviewRequest(11,request,true,""));
        assertEquals(request,pendingRequest());
    }

    @Test void approvalNotificationFailureRollsBackMembershipAndReview() throws Exception {
        unassignedShipper();teams.applyToShop(21,1);
        int request=pendingRequest();
        db.exec("ALTER TABLE notifications ADD CONSTRAINT fail_shipper CHECK(user_id<>21)");
        assertThrows(SQLException.class,()->teams.reviewRequest(10,request,true,""));
        assertEquals(request,pendingRequest());
        assertNull(teams.getMyMembership(21));
        assertEquals(0,db.count("SELECT COUNT(*) FROM shop_delivery_settings WHERE shipper_id=21"));
    }

    @Test void suspendedShopCannotApprovePendingRequest() throws Exception {
        unassignedShipper();teams.applyToShop(21,1);
        int request=pendingRequest();db.exec("UPDATE shops SET status='suspended' WHERE shop_id=1");
        assertThrows(IllegalArgumentException.class,()->teams.reviewRequest(10,request,true,""));
        assertNull(teams.getMyMembership(21));
    }

    @Test void withdrawnRequestCannotBeApproved() throws Exception {
        unassignedShipper();teams.applyToShop(21,1);int request=pendingRequest();
        teams.withdrawRequest(21,request);
        assertEquals("Withdrawn",teams.getMyRequests(21).get(0).getStatus());
        assertThrows(IllegalArgumentException.class,()->teams.reviewRequest(10,request,true,""));
    }
}
