package com.cinebee.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST(400, "Bad request", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

    // User-related errors
    USER_EXISTED(400, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(404, "User does not exist", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1001, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1002, "Invalid email format", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH(1004, "Password and confirm password do not match", HttpStatus.BAD_REQUEST),
    INVALID_DOB(1005, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(1006, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),

    // Auth / Token errors
    TOKEN_NOT_FOUND(2001, "Token not found", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(2002, "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(2003, "Invalid token", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(2004, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2005, "You do not have permission", HttpStatus.FORBIDDEN),

    // File upload / download errors
    FILE_EMPTY(3001, "File must not be empty", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(3002, "File size exceeds the limit", HttpStatus.BAD_REQUEST),
    FILE_TYPE_NOT_SUPPORTED(3003, "Unsupported file type", HttpStatus.BAD_REQUEST),

    // Validation errors
    REQUIRED_FIELD_MISSING(4001, "Required field is missing: {field}", HttpStatus.BAD_REQUEST),
    INVALID_ENUM_VALUE(4002, "Invalid value for field: {field}", HttpStatus.BAD_REQUEST),
    VALUE_OUT_OF_RANGE(4003, "Value of {field} must be between {min} and {max}", HttpStatus.BAD_REQUEST),
    STRING_TOO_SHORT(4004, "Length of {field} must be at least {min} characters", HttpStatus.BAD_REQUEST),
    STRING_TOO_LONG(4005, "Length of {field} must be at most {max} characters", HttpStatus.BAD_REQUEST),
    NUMBER_TOO_SMALL(4006, "Value of {field} must be at least {min}", HttpStatus.BAD_REQUEST),
    NUMBER_TOO_LARGE(4007, "Value of {field} must be at most {max}", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT(4008, "Invalid date format for {field}. Expected format: {format}", HttpStatus.BAD_REQUEST),

    // Database errors
    DATA_INTEGRITY_VIOLATION(5001, "Data integrity violation", HttpStatus.CONFLICT),
    DUPLICATE_KEY(5002, "Duplicate key value violates unique constraint", HttpStatus.CONFLICT),

    // Other possible errors
    SERVICE_UNAVAILABLE(6001, "Service is temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    TOO_MANY_REQUESTS(6002, "Too many requests - try again later", HttpStatus.TOO_MANY_REQUESTS),
    REFRESH_TOKEN_LIMIT_EXCEEDED(6003, "Refresh token has exceeded the allowed number of uses. Please login again.", HttpStatus.UNAUTHORIZED),

    // Movie-related errors
    MOVIE_NOT_FOUND(7001, "Movie not found", HttpStatus.NOT_FOUND),
    MOVIE_IMAGE_UPLOAD_FAILED(7002, "Failed to upload movie image", HttpStatus.INTERNAL_SERVER_ERROR),
    MOVIE_IMAGE_DELETE_FAILED(7003, "Failed to delete old movie image", HttpStatus.INTERNAL_SERVER_ERROR),

    // Banner-related errors
    BANNER_NOT_FOUND(8006, "Banner not found", HttpStatus.NOT_FOUND),

    // Authentication service errors
    CAPTCHA_INVALID(8001, "Captcha is incorrect or expired", HttpStatus.BAD_REQUEST),
    USERNAME_OR_PHONE_INVALID(8002, "Username/email/phone is required and must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PHONE_INVALID_FORMAT(8005, "Invalid Vietnamese phone number format (10 digits, starts with 0)", HttpStatus.BAD_REQUEST),

    // Ticket and Payment errors
    TICKET_NOT_FOUND(9001, "Ticket not found", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_COMPLETED(9002, "This ticket has already been paid for", HttpStatus.CONFLICT),
    PAYMENT_CREATION_FAILED(9003, "Failed to create payment request with the provider", HttpStatus.INTERNAL_SERVER_ERROR),
    SEAT_NOT_AVAILABLE(9004, "Seat {seatNumber} is not available for this showtime", HttpStatus.CONFLICT),
    SHOWTIME_NOT_FOUND(9005, "Showtime not found", HttpStatus.NOT_FOUND),
    BOOKING_ALREADY_CANCELLED(9006, "This booking has already been cancelled", HttpStatus.CONFLICT),
    BOOKING_ALREADY_PAID(9007, "Cannot cancel a paid booking", HttpStatus.CONFLICT),
    SHOWTIME_HAS_PASSED(9008, "Cannot book a showtime that has already passed", HttpStatus.BAD_REQUEST),
    SEAT_NOT_FOUND(9009, "Seat not found", HttpStatus.NOT_FOUND),
    CANCELLATION_WINDOW_EXPIRED(9010, "Cannot cancel booking after 1 hour", HttpStatus.BAD_REQUEST),
    REFUND_FAILED(9011, "Failed to process refund", HttpStatus.INTERNAL_SERVER_ERROR),

    // Theater errors
    THEATER_NOT_FOUND(10001, "Theater not found", HttpStatus.NOT_FOUND);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
        this.errorCode = name();
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
    private final String errorCode;
}
