import cv2
import numpy as np
import pyrealsense2 as rs

# Configure depth and color streams
pipeline = rs.pipeline()
config = rs.config()
config.enable_stream(rs.stream.color, 640, 480, rs.format.bgr8, 30)

# Start the pipeline
pipeline.start(config)

try:
    while True:
        # Wait for a coherent pair of frames: depth and color
        frames = pipeline.wait_for_frames()
        color_frame = frames.get_color_frame()
        if not color_frame:
            continue

        # Convert images to numpy arrays
        imageFrame = np.asanyarray(color_frame.get_data())

        # Convert imageFrame to HSV (hue-saturation-value)
        hsvFrame = cv2.cvtColor(imageFrame, cv2.COLOR_BGR2HSV)

        # Define the range of each color in HSV
        blue_lower = np.array([94, 80, 2], np.uint8)
        blue_upper = np.array([120, 255, 255], np.uint8)
        red_lower = np.array([136, 87, 111], np.uint8)
        red_upper = np.array([180, 255, 255], np.uint8)
        yellow_lower = np.array([22, 93, 0], np.uint8)
        yellow_upper = np.array([45, 255, 255], np.uint8)
        green_lower = np.array([50, 100, 50], np.uint8)
        green_upper = np.array([70, 255, 255], np.uint8)
        purple_lower = np.array([130, 0, 0], np.uint8)
        purple_upper = np.array([150, 255, 255], np.uint8)

        # Masks for each color
        blue_mask = cv2.inRange(hsvFrame, blue_lower, blue_upper)
        red_mask = cv2.inRange(hsvFrame, red_lower, red_upper)
        yellow_mask = cv2.inRange(hsvFrame, yellow_lower, yellow_upper)
        green_mask = cv2.inRange(hsvFrame, green_lower, green_upper)
        purple_mask = cv2.inRange(hsvFrame, purple_lower, purple_upper)

        kernel = np.ones((5, 5), "uint8")

        # Dilation for each color mask
        blue_mask = cv2.dilate(blue_mask, kernel)
        red_mask = cv2.dilate(red_mask, kernel)
        yellow_mask = cv2.dilate(yellow_mask, kernel)
        green_mask = cv2.dilate(green_mask, kernel)
        purple_mask = cv2.dilate(purple_mask, kernel)

        # Contour for each color
        for color, mask, text, color_val in [("Blue", blue_mask, "Blue Colour", (255, 0, 0)),
                                             ("Red", red_mask, "Red Colour", (0, 0, 255)),
                                             ("Yellow", yellow_mask, "Yellow Colour", (0, 255, 255)),
                                             ("Green", green_mask, "Green Colour", (0, 255, 0)),
                                             ("Purple", purple_mask, "Purple Colour", (128, 0, 128))]:
            contours, _ = cv2.findContours(mask, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
            for contour in contours:
                area = cv2.contourArea(contour)
                if area > 300:
                    x, y, w, h = cv2.boundingRect(contour)
                    imageFrame = cv2.rectangle(imageFrame, (x, y), (x + w, y + h), color_val, 2)
                    cv2.putText(imageFrame, text, (x, y), cv2.FONT_HERSHEY_SIMPLEX, 1.0, color_val)

        # Display the result
        cv2.imshow("Multiple Color Detection in Real-Time", imageFrame)

        # Break the loop when 'q' is pressed
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

finally:
    # Stop the pipeline
    pipeline.stop()
    cv2.destroyAllWindows()
