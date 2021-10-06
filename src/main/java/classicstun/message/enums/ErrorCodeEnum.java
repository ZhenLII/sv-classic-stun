package classicstun.message.enums;

/**
 * @author JiangZhenli
 */
public enum ErrorCodeEnum {
    // 错误码
    BadRequest(400,"Bad Request"),
    Unauthorized(401,"Unauthorized"),
    UnknownAttribute(420,"Unknown Attribute"),
    StaleCredentials(430,"Stale Credentials"),
    IntegrityCheckFailure(431,"Integrity Check Failure"),
    MissingUsername(432,"Missing Username"),
    UseTLS(433,"Use TLS"),
    ServerError(500,"Server Error"),
    GlobalFailure(600,"Global Failure");

    int code;
    String reason;
    ErrorCodeEnum(int code,String reason) {
        this.code = code;
        this.reason = reason;
    }
}
