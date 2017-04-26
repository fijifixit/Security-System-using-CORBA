import java.util.TimerTask;

public class CancelTimer extends TimerTask {
	HomeHubServant rel;
	public CancelTimer(HomeHubServant newHub){
		rel = newHub;
	}

	@Override
	public void run() {
		if(rel.getIsHit()){
			rel.setIsHit(false);
		}

		rel.CancelTimer();
	}
}
