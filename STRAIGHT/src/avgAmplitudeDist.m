function dist = avgAmplitudeDist(filename1, filename2)
    x = avgAmplitude(filename1);
    y = avgAmplitude(filename2);
    dist = abs(x-y);
    fprintf(sprintf('(%.2f %.2f)  %.2f, %.2f\n', x, y, dist, y/x));
end

function res = avgAmplitude(filename)
    [y, Fs] = wavread(filename);
    len = length(y);
    Y = fft(y, len); 
    half = abs(Y(1:floor(len/2)));
    res = mean(half);
end