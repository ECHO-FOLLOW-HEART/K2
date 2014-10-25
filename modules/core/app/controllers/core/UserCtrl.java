package controllers.core;

import aizou.core.UserAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zephyre on 10/24/14.
 */
public class UserCtrl extends Controller {

    public static Result addEaseMobContacts(Integer userA, Integer userB, boolean actionAdd) {
        if (userA == null || userB == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");

        try {
            UserAPI.modEaseMobContacts(userA, userB, actionAdd);
            return Utils.createResponse(ErrorCode.NORMAL, "");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    public static Result modEaseMobBlocks(Integer userA, Integer userB, boolean actionAdd) {
        if (userA == null || userB == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");

        try {
            if (actionAdd) {
                List<Integer> blockIds = new ArrayList<>();
                blockIds.add(userB);
                UserAPI.addEaseMobBlocks(userA, blockIds);
            } else {
                UserAPI.delEaseMobBlocks(userA, userB);
            }
            return Utils.createResponse(ErrorCode.NORMAL, "");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }




}
