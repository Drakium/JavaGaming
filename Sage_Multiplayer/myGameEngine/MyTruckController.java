package myGameEngine;

import graphicslib3D.Matrix3D;
import sage.scene.Controller;
import sage.scene.SceneNode;

public class MyTruckController extends Controller {

	private double translationRate = 0.02; // movement per second
	private double cycleTime = 1000.0; 
	private double totalTime;
	private double direction = 1.0;
	
	public void setCycleTime(double c) {
		cycleTime = c;
	}
	
	@Override
	public void update(double time) { // example controller
		totalTime += time;
		double transAmount = translationRate * time;
		
		if(totalTime > cycleTime) {
			direction = -direction;
			totalTime = 0.0;
		}
		
		transAmount = direction * transAmount;
		
		Matrix3D newTrans = new Matrix3D();
		newTrans.rotate(transAmount, transAmount, transAmount);
		//newTrans.scale(transAmount, 1, 1);
		for (SceneNode node : controlledNodes) {
			Matrix3D curTrans = node.getLocalRotation();
			curTrans.concatenate(newTrans);
			node.setLocalRotation(curTrans);
		}
	}
	
}
