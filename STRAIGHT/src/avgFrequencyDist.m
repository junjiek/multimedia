function dist = avgFrequencyDist(filename1, filename2)
    x = avgFrequency(filename1);
    y = avgFrequency(filename2);
    dist = abs(x-y);
    fprintf(sprintf('(%.2f %.2f)  %.2f, %.2f\n', x, y, dist, y/x));
end

function res = avgFrequency(filename)
    [y, Fs] = wavread(filename); 
    len = length(y);
    Y = fft(y,len);
    half = abs(Y(1:floor(len/2)));
    res = 0;
    for i = 1:1:length(half)
        res = res + half(i) * i;
    end
    disp(sum(half));
    res = res / sum(half);
    res = res * Fs/len;
end