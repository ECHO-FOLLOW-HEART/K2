package controllers.core;

import aizou.core.UserAPI;
import exception.AizouException;
import exception.ErrorCode;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zephyre on 10/24/14.
 */
public class UserCtrl extends Controller {

    public static Result addEaseMobContacts(Long userA, Long userB, boolean actionAdd) {
        if (userA == null || userB == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");

        try {
            UserAPI.modEaseMobContacts(userA, userB, actionAdd);
            return Utils.createResponse(ErrorCode.NORMAL, "");
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    public static Result modEaseMobBlocks(Long userA, Long userB, boolean actionAdd) {
        if (userA == null || userB == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");

        try {
            if (actionAdd) {
                List<Long> blockIds = new ArrayList<>();
                blockIds.add(userB);
                UserAPI.addEaseMobBlocks(userA, blockIds);
            } else {
                UserAPI.delEaseMobBlocks(userA, userB);
            }
            return Utils.createResponse(ErrorCode.NORMAL, "");
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }


}
