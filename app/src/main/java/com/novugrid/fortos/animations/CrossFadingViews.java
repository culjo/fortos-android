/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;

/**
 * Created by WeaverBird on 4/20/2016.
 * @author WeaverBird
 *
 */
public class CrossFadingViews {

    public static final int SHORT_LENGTH = 1;
    public static final int MIDDLE_LENGTH = 2;
    public static final int LONG_LENGTH = 3;

    public static int durationResource(Context context, int length){
        if(context == null) return 0;// stop the execution here

        int mLength = 0;

        switch (length){
            case SHORT_LENGTH:
                mLength = android.R.integer.config_shortAnimTime;
                break;
            case MIDDLE_LENGTH:
                mLength = android.R.integer.config_mediumAnimTime   ;
                break;
            case LONG_LENGTH:
                mLength = android.R.integer.config_longAnimTime;
                break;
            default:
                mLength = android.R.integer.config_longAnimTime;

        }

        return context.getResources().getInteger(mLength);

    }

    /**
     * This switches between two views by a fading effect
     * @param view1
     * @param view2
     * @param duration
     */
    public static void crossFade(final View view1, View view2, int duration){

        final View comingView, goingView;

        if(view1.getVisibility() == View.GONE){

            comingView = view1;
            goingView = view2;

        }else {

            comingView = view2;
            goingView = view1;

        }

        // Fade out the goingView
        // Animate the other view to 0% Opacity. AFter the animation ends, set its visibility
        // to GONE as an optimization step (it wont paticipate in layout passes, etc..)
        goingView.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        goingView.setVisibility(View.GONE);
                    }
                });


        // Fade in the commingView
        comingView.setAlpha(0);
        comingView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity and clear any animation
        // listener set on the view
        comingView.animate().alpha(1f)
                .setDuration(duration)
                .setListener(null);



    }

    public static void fadeIn(final View comingView, final View goingView, int duration){

        // Fade out the goingView
        // Animate the other view to 0% Opacity. AFter the animation ends, set its visibility
        // to GONE as an optimization step (it wont paticipate in layout passes, etc..)
        goingView.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        goingView.setVisibility(View.GONE);
                    }
                });


        // Fade in the commingView
        comingView.setAlpha(0);
        comingView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity and clear any animation
        // listener set on the view
        comingView.animate().alpha(1f)
                .setDuration(duration)
                .setListener(null);



    }

    public static void fadeIn(View comingView, int duration){

        if(comingView.getVisibility() == View.GONE || comingView.getVisibility() == View.INVISIBLE){

            comingView.setAlpha(0);
            comingView.setVisibility(View.VISIBLE);

            // This does the fading in
            // Animate the content view to 100% opacity and clear any animation
            // listener set on the view
            comingView.animate().alpha(1f)
                    .setDuration(duration)
                    .setListener(null);


        }

    }

    public static void fadeOut(final View goingView, int duration){

        if(goingView.getVisibility() == View.VISIBLE){

            // Fade out the goingView
            // Animate the other view to 0% Opacity. AFter the animation ends, set its visibility
            // to GONE as an optimization step (it wont paticipate in layout passes, etc..)
            goingView.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            goingView.setVisibility(View.GONE);
                        }
                    });


        }

    }

}
