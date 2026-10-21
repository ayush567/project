package interfaces;

// Interface for entities that maintain tracking and status information
public interface Trackable {
    String getMemberId();
    String getStatus();
    void setStatus(String status);
    String getTrackingSummary();
}