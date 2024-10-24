import cv2 as cv
from itertools import zip_longest
import numpy as np


"""Purpose: Tracks multiple items using a region of interest algorithm. Users are prompted to draw
shape around object they want to detect and algorithm draws a bounding box around said object.
Result: Tracks multiple objects but must be manually selected by user. Number of items tracked can 
be change in for loop in line 18.
"""

bboxes = []
cap = cv.VideoCapture(1)


ret, frame = cap.read()

for i in range(3):
    bbox = cv.selectROI("Frame", frame, fromCenter=False, showCrosshair=True)
    bboxes.append(bbox)

multi_tracker = cv.legacy.MultiTracker_create()
for box in bboxes:
     multi_tracker.add(cv.legacy.TrackerCSRT_create(), frame, box)


print("Multitracker created")


while cap.isOpened():
    #Read each frame
    ret, frame = cap.read()
    if not ret:
            break
        
    ret, boxes = multi_tracker.update(frame)
    # Draw bounding box around the tracked object
    """
    if ret:
        for i in range(len(bboxes)):
            x, y, w, h = map(int, bboxes[i])
            cv.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
    """
    for i, newbox in enumerate(boxes):
        p1 = (int(newbox[0]), int(newbox[1]))
        p2 = (int(newbox[0] + newbox[2]), int(newbox[1] + newbox[3]))
        cv.rectangle(frame, p1, p2, (0, 255, 0), 2, 1)
        cv.putText(frame, 'Ring ' + str(i), (p1[0], p1[1]-10), cv.FONT_HERSHEY_SIMPLEX, 0.9, (255, 0, 0), 2)

            
    cv.imshow("MultiTracker", frame)

    key = cv.waitKey(30)

    if key == 27:
        break
    
cap.release()
cv.destroyAllWindows()
